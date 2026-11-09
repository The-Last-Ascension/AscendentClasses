package com.thelastascension.ascendentClasses.manager;

import com.thelastascension.ascendentClasses.AscendentClasses;
import com.thelastascension.ascendentClasses.model.ClassSkill;
import com.thelastascension.ascendentClasses.model.PlayerClassProgress;
import com.thelastascension.ascendentClasses.model.RpgClassDefinition;
import com.thelastascension.ascendentClasses.model.SkillCategory;
import com.thelastascension.ascendentClasses.model.TalismanDefinition;
import com.thelastascension.ascendentCombat.AscendentCombat;
import com.thelastascension.ascendentCore.i18n.LangManager;
import com.thelastascension.ascendentCore.model.ElementType;
import com.thelastascension.ascendentCore.model.PlayerProfile;
import com.thelastascension.ascendentCore.model.StatType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Gestiona la persistencia SQLite/MySQL y la lógica de:
 * - Progreso independiente por Clase (estilo Trove) + Elección Starter (estilo Pokémon).
 * - Puntos de Estadística Globales (persisten entre clases) vs Puntos de Habilidad de Clase.
 * - Armaduras con Estadísticas Base Universales + Pasivas Exclusivas de Clase.
 * - Talismanes equipables por ranuras desbloqueables (estilo Elden Ring).
 * - Lanzamiento y mejora evolutiva de Habilidades (Niveles 1..5).
 */
public class ClassAndGearManager implements Listener {

    private final AscendentClasses plugin;

    // UUID Jugador -> (ClassId -> Progreso de esa clase)
    private final Map<UUID, Map<String, PlayerClassProgress>> classProgressCache = new ConcurrentHashMap<>();
    // UUID Jugador -> Lista de IDs de Talismanes equipados
    private final Map<UUID, List<String>> equippedTalismansCache = new ConcurrentHashMap<>();
    // UUID Jugador -> (SkillId -> Timestamp ms fin de cooldown)
    private final Map<UUID, Map<String, Long>> skillCooldowns = new ConcurrentHashMap<>();

    // PDC Keys para Armaduras con Stats Universales + Pasivas Exclusivas de Clase
    private final NamespacedKey keyUnivHealth;
    private final NamespacedKey keyUnivDefense;
    private final NamespacedKey keyReqClass;
    private final NamespacedKey keyClassStat;
    private final NamespacedKey keyClassBonus;

    public ClassAndGearManager(AscendentClasses plugin) {
        this.plugin = plugin;
        this.keyUnivHealth = new NamespacedKey(plugin, "univ_health");
        this.keyUnivDefense = new NamespacedKey(plugin, "univ_defense");
        this.keyReqClass = new NamespacedKey(plugin, "req_class");
        this.keyClassStat = new NamespacedKey(plugin, "class_stat");
        this.keyClassBonus = new NamespacedKey(plugin, "class_bonus");
    }

    public void initializeDatabase() {
        String createClassTable = """
                CREATE TABLE IF NOT EXISTS ascendent_class_progress (
                    uuid VARCHAR(36) NOT NULL,
                    class_id VARCHAR(64) NOT NULL,
                    class_level INT NOT NULL DEFAULT 1,
                    class_exp DOUBLE NOT NULL DEFAULT 0.0,
                    free_skill_points INT NOT NULL DEFAULT 2,
                    skill_levels_data TEXT NOT NULL DEFAULT '',
                    unlocked_slots_data TEXT NOT NULL DEFAULT '',
                    PRIMARY KEY (uuid, class_id)
                );
                """;
        String createGearTable = """
                CREATE TABLE IF NOT EXISTS ascendent_player_gear (
                    uuid VARCHAR(36) PRIMARY KEY,
                    equipped_talismans TEXT NOT NULL DEFAULT ''
                );
                """;
        plugin.getCore().getDatabaseManager().executeSchema(createClassTable);
        plugin.getCore().getDatabaseManager().executeSchema(createGearTable);

        for (Player online : Bukkit.getOnlinePlayers()) {
            loadPlayerDataAsync(online);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadPlayerDataAsync(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getCore().getDatabaseManager().runAsync(() -> {
            savePlayerDataSync(uuid);
            classProgressCache.remove(uuid);
            equippedTalismansCache.remove(uuid);
            skillCooldowns.remove(uuid);
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            recalculateAndApplyStats(player);
        }
    }

    public void loadPlayerDataAsync(Player player) {
        UUID uuid = player.getUniqueId();
        plugin.getCore().getDatabaseManager().runAsync(() -> {
            Map<String, PlayerClassProgress> map = new ConcurrentHashMap<>();
            List<String> talismans = new ArrayList<>();

            try (Connection conn = plugin.getCore().getDatabaseManager().getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM ascendent_class_progress WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String classId = rs.getString("class_id").toLowerCase(Locale.ROOT);
                            PlayerClassProgress prog = new PlayerClassProgress(classId);
                            prog.setClassLevel(rs.getInt("class_level"));
                            prog.setClassExp(rs.getDouble("class_exp"));
                            prog.setFreeSkillPoints(rs.getInt("free_skill_points"));
                            deserializeSkillLevels(rs.getString("skill_levels_data"), prog);
                            deserializeUnlockedSlots(rs.getString("unlocked_slots_data"), prog);
                            map.put(classId, prog);
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement("SELECT equipped_talismans FROM ascendent_player_gear WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String raw = rs.getString("equipped_talismans");
                            if (raw != null && !raw.isBlank()) {
                                for (String t : raw.split(",")) {
                                    if (!t.isBlank()) talismans.add(t.trim().toLowerCase(Locale.ROOT));
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error cargando datos de clases para " + player.getName(), e);
            }

            classProgressCache.put(uuid, map);
            equippedTalismansCache.put(uuid, talismans);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    recalculateAndApplyStats(player);
                }
            });
        });
    }

    public void saveAllSync() {
        for (UUID uuid : classProgressCache.keySet()) {
            savePlayerDataSync(uuid);
        }
    }

    public void savePlayerDataSync(UUID uuid) {
        Map<String, PlayerClassProgress> classMap = classProgressCache.get(uuid);
        List<String> talismans = equippedTalismansCache.get(uuid);
        if (classMap == null && talismans == null) return;

        try (Connection conn = plugin.getCore().getDatabaseManager().getConnection()) {
            if (classMap != null) {
                String sql = """
                        INSERT INTO ascendent_class_progress
                        (uuid, class_id, class_level, class_exp, free_skill_points, skill_levels_data, unlocked_slots_data)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT(uuid, class_id) DO UPDATE SET
                            class_level = excluded.class_level,
                            class_exp = excluded.class_exp,
                            free_skill_points = excluded.free_skill_points,
                            skill_levels_data = excluded.skill_levels_data,
                            unlocked_slots_data = excluded.unlocked_slots_data;
                        """;
                if (plugin.getCore().getDatabaseManager().isMysqlMode()) {
                    sql = """
                            INSERT INTO ascendent_class_progress
                            (uuid, class_id, class_level, class_exp, free_skill_points, skill_levels_data, unlocked_slots_data)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE
                                class_level = VALUES(class_level),
                                class_exp = VALUES(class_exp),
                                free_skill_points = VALUES(free_skill_points),
                                skill_levels_data = VALUES(skill_levels_data),
                                unlocked_slots_data = VALUES(unlocked_slots_data);
                            """;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (PlayerClassProgress prog : classMap.values()) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, prog.getClassId());
                        ps.setInt(3, prog.getClassLevel());
                        ps.setDouble(4, prog.getClassExp());
                        ps.setInt(5, prog.getFreeSkillPoints());
                        ps.setString(6, serializeSkillLevels(prog));
                        ps.setString(7, serializeUnlockedSlots(prog));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            if (talismans != null) {
                String talismanStr = String.join(",", talismans);
                String gearSql = """
                        INSERT INTO ascendent_player_gear (uuid, equipped_talismans)
                        VALUES (?, ?)
                        ON CONFLICT(uuid) DO UPDATE SET equipped_talismans = excluded.equipped_talismans;
                        """;
                if (plugin.getCore().getDatabaseManager().isMysqlMode()) {
                    gearSql = """
                            INSERT INTO ascendent_player_gear (uuid, equipped_talismans)
                            VALUES (?, ?)
                            ON DUPLICATE KEY UPDATE equipped_talismans = VALUES(equipped_talismans);
                            """;
                }
                try (PreparedStatement ps = conn.prepareStatement(gearSql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, talismanStr);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando progreso de clases para " + uuid, e);
        }
    }

    public PlayerClassProgress getOrCreateClassProgress(UUID uuid, String classId) {
        Map<String, PlayerClassProgress> map = classProgressCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        return map.computeIfAbsent(classId.toLowerCase(Locale.ROOT), PlayerClassProgress::new);
    }

    public List<String> getEquippedTalismans(UUID uuid) {
        return equippedTalismansCache.computeIfAbsent(uuid, k -> new ArrayList<>());
    }

    /**
     * Cambia libremente de clase (estilo Trove), conservando las estadísticas globales
     * y cargando el nivel y puntos de habilidad propios de la nueva clase.
     */
    public boolean switchClass(Player player, String targetClassId) {
        RpgClassDefinition targetDef = plugin.getRegistry().getClass(targetClassId);
        if (targetDef == null) return false;

        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return false;

        if (targetDef.id().equalsIgnoreCase(profile.getActiveClassId())) {
            plugin.getCore().getLang().send(player, "classes.already-active", LangManager.placeholders("class", targetDef.nameEs()));
            return false;
        }

        // Si es una evolución/ascensión avanzada, comprobar que tenga el nivel requerido en la clase base
        if (!targetDef.isStarter()) {
            int reqLevel = plugin.getConfig().getInt("classes.ascension-required-class-level", 10);
            PlayerClassProgress baseProg = getOrCreateClassProgress(player.getUniqueId(), targetDef.parentStarterId());
            if (baseProg.getClassLevel() < reqLevel) {
                RpgClassDefinition baseDef = plugin.getRegistry().getClass(targetDef.parentStarterId());
                plugin.getCore().getLang().send(player, "classes.ascension-locked", LangManager.placeholders(
                        "req", String.valueOf(reqLevel),
                        "base", baseDef != null ? baseDef.nameEs() : targetDef.parentStarterId(),
                        "class", targetDef.nameEs()
                ));
                return false;
            }
        }

        profile.setActiveClassId(targetDef.id());
        PlayerClassProgress newProg = getOrCreateClassProgress(player.getUniqueId(), targetDef.id());

        recalculateAndApplyStats(player);
        plugin.getCore().getDatabaseManager().runAsync(() -> plugin.getCore().getProfileManager().saveToDatabaseSync(profile));
        plugin.getCore().getDatabaseManager().runAsync(() -> savePlayerDataSync(player.getUniqueId()));

        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.85f, 1.25f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.0, 0), 30, 0.4, 0.6, 0.4, 0.08);

        plugin.getCore().getLang().send(player, "classes.switched", LangManager.placeholders(
                "class", targetDef.nameEs(),
                "level", String.valueOf(newProg.getClassLevel()),
                "points", String.valueOf(newProg.getFreeSkillPoints())
        ));
        return true;
    }

    /**
     * Invierte Puntos de Estadística Globales (persisten aunque cambies de clase).
     */
    public boolean investGlobalStatPoint(Player player, StatType stat, int amount) {
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return false;

        int available = profile.getFreeStatPoints();
        if (available <= 0) {
            plugin.getCore().getLang().send(player, "classes.stat-no-points");
            return false;
        }

        int toSpend = Math.min(available, Math.max(1, amount));
        profile.setFreeStatPoints(available - toSpend);
        int newTotal = profile.getInvestedPoints(stat) + toSpend;
        profile.setInvestedPoints(stat, newTotal);

        recalculateAndApplyStats(player);
        plugin.getCore().getDatabaseManager().runAsync(() -> plugin.getCore().getProfileManager().saveToDatabaseSync(profile));

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9f, 1.4f);
        plugin.getCore().getLang().send(player, "classes.stat-invested", LangManager.placeholders(
                "stat", stat.getDisplayNameEs(),
                "points", String.valueOf(newTotal),
                "free", String.valueOf(profile.getFreeStatPoints())
        ));
        return true;
    }

    /**
     * Sube de nivel una habilidad de la clase activa (aumentando daño, Stun y rango AoE).
     */
    public boolean upgradeClassSkill(Player player, String skillId) {
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return false;

        RpgClassDefinition activeClass = plugin.getRegistry().getClass(profile.getActiveClassId());
        if (activeClass == null) return false;

        ClassSkill targetSkill = activeClass.skills().stream()
                .filter(s -> s.id().equalsIgnoreCase(skillId))
                .findFirst().orElse(null);
        if (targetSkill == null) return false;

        PlayerClassProgress prog = getOrCreateClassProgress(player.getUniqueId(), activeClass.id());
        int maxLevel = plugin.getConfig().getInt("skills.max-skill-level", 5);
        int currentLevel = prog.getSkillLevel(targetSkill.id());

        if (currentLevel >= maxLevel) {
            plugin.getCore().getLang().send(player, "classes.skill-max-level", LangManager.placeholders(
                    "skill", targetSkill.nameEs(),
                    "max", String.valueOf(maxLevel)
            ));
            return false;
        }
        if (prog.getFreeSkillPoints() <= 0) {
            plugin.getCore().getLang().send(player, "classes.skill-no-points", LangManager.placeholders(
                    "class", activeClass.nameEs()
            ));
            return false;
        }

        int nextLevel = currentLevel + 1;
        prog.setFreeSkillPoints(prog.getFreeSkillPoints() - 1);
        prog.setSkillLevel(targetSkill.id(), nextLevel);
        plugin.getCore().getDatabaseManager().runAsync(() -> savePlayerDataSync(player.getUniqueId()));

        double dmgGrowth = plugin.getConfig().getDouble("skills.damage-growth-per-level", 0.25);
        double aoeGrowth = plugin.getConfig().getDouble("skills.aoe-growth-per-level", 0.60);

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.3f);
        plugin.getCore().getLang().send(player, "classes.skill-upgraded", LangManager.placeholders(
                "skill", targetSkill.nameEs(),
                "level", String.valueOf(nextLevel),
                "max", String.valueOf(maxLevel),
                "dmg", String.format("%.0f", (nextLevel - 1) * dmgGrowth * 100.0),
                "aoe", String.format("%.1f", targetSkill.getScaledAoeRadius(nextLevel, aoeGrowth))
        ));
        return true;
    }

    /**
     * Lanza una habilidad de la clase activa aplicando su daño, Stun, AoE escalado por nivel y sinergias elementales.
     */
    public boolean castSkill(Player player, ClassSkill skill) {
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null || profile.isStunned() || profile.isDowned()) return false;

        Map<String, Long> cdMap = skillCooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        long readyAt = cdMap.getOrDefault(skill.id(), 0L);
        if (now < readyAt) {
            long remSec = Math.max(1L, (readyAt - now + 999L) / 1000L);
            plugin.getCore().getLang().send(player, "classes.skill-cooldown", LangManager.placeholders(
                    "skill", skill.nameEs(),
                    "seconds", String.valueOf(remSec)
            ));
            return false;
        }

        PlayerClassProgress prog = getOrCreateClassProgress(player.getUniqueId(), profile.getActiveClassId());
        int level = prog.getSkillLevel(skill.id());
        double dmgGrowth = plugin.getConfig().getDouble("skills.damage-growth-per-level", 0.25);
        double aoeGrowth = plugin.getConfig().getDouble("skills.aoe-growth-per-level", 0.60);

        double totalDamage = skill.getScaledDamage(level, dmgGrowth) + (profile.getStat(StatType.MAGIC_DAMAGE) * 0.35);
        double totalStun = skill.getScaledStun(level);
        double radius = skill.getScaledAoeRadius(level, aoeGrowth);

        cdMap.put(skill.id(), now + (skill.cooldownSeconds() * 1000L));

        // Movimiento especial si es de categoría MOVIMIENTO
        if (skill.category() == SkillCategory.MOVIMIENTO) {
            player.setVelocity(player.getLocation().getDirection().normalize().multiply(1.15 + (level * 0.12)).setY(0.35));
        }

        Location center = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(1.8));
        spawnSkillEffect(center, skill.element(), level, radius);

        AscendentCombat combatPlugin = getCombatPlugin();
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !target.equals(player) && !(target instanceof Player)) {
                target.damage(totalDamage, player);
                if (combatPlugin != null) {
                    combatPlugin.getParryAndStunManager().addEnemyStun(target, totalStun, player);
                }
            }
        }

        // Si es una DEFINITIVA, abre/detona ventana de Definitiva Conjunta Cooperativa (Tipo 2) en AscendentCombat
        if (skill.category() == SkillCategory.DEFINITIVA && combatPlugin != null && skill.element() != null) {
            combatPlugin.getElementalReactionManager().triggerCoopSkillSynergy(player, skill.element(), false);
        }

        plugin.getCore().getLang().send(player, "classes.skill-cast", LangManager.placeholders(
                "skill", skill.nameEs(),
                "level", String.valueOf(level),
                "damage", String.format("%.0f", totalDamage),
                "stun", String.format("%.0f", totalStun),
                "aoe", String.format("%.1f", radius)
        ));
        return true;
    }

    /**
     * Equipa o desequipa un Talismán estilo Elden Ring respetando las ranuras desbloqueadas.
     */
    public boolean toggleTalisman(Player player, String talismanId) {
        TalismanDefinition def = plugin.getRegistry().getTalisman(talismanId);
        if (def == null) return false;

        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return false;

        List<String> equipped = getEquippedTalismans(player.getUniqueId());
        String key = def.id().toLowerCase(Locale.ROOT);

        if (equipped.contains(key)) {
            equipped.remove(key);
            recalculateAndApplyStats(player);
            plugin.getCore().getDatabaseManager().runAsync(() -> savePlayerDataSync(player.getUniqueId()));
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.9f, 0.85f);
            plugin.getCore().getLang().send(player, "classes.talisman-unequipped", LangManager.placeholders("talisman", def.nameEs()));
            return true;
        }

        int maxSlots = profile.getUnlockedTalismanSlots();
        if (equipped.size() >= maxSlots) {
            plugin.getCore().getLang().send(player, "classes.talisman-slots-full", LangManager.placeholders("max", String.valueOf(maxSlots)));
            return false;
        }

        equipped.add(key);
        recalculateAndApplyStats(player);
        plugin.getCore().getDatabaseManager().runAsync(() -> savePlayerDataSync(player.getUniqueId()));
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1.0f, 1.25f);
        plugin.getCore().getLang().send(player, "classes.talisman-equipped", LangManager.placeholders(
                "talisman", def.nameEs(),
                "used", String.valueOf(equipped.size()),
                "max", String.valueOf(maxSlots)
        ));
        return true;
    }

    /**
     * Resetea (Respec) los Puntos de Estadística Globales o los Puntos de Habilidad de la Clase activa.
     */
    public boolean respecPoints(Player player, boolean globalStats) {
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return false;

        long cost = plugin.getConfig().getLong("classes.respec-coin-cost", 250L);
        if (profile.getCoins() < cost) {
            plugin.getCore().getLang().send(player, "classes.respec-no-coins", LangManager.placeholders("cost", String.valueOf(cost)));
            return false;
        }

        if (globalStats) {
            int refunded = 0;
            for (StatType stat : StatType.values()) {
                refunded += profile.getInvestedPoints(stat);
                profile.setInvestedPoints(stat, 0);
            }
            if (refunded <= 0) return false;
            profile.setCoins(profile.getCoins() - cost);
            profile.setFreeStatPoints(profile.getFreeStatPoints() + refunded);
            recalculateAndApplyStats(player);
            plugin.getCore().getDatabaseManager().runAsync(() -> plugin.getCore().getProfileManager().saveToDatabaseSync(profile));
            plugin.getCore().getLang().send(player, "classes.respec-stats-done", LangManager.placeholders("points", String.valueOf(refunded)));
            return true;
        } else {
            RpgClassDefinition activeClass = plugin.getRegistry().getClass(profile.getActiveClassId());
            if (activeClass == null) return false;
            PlayerClassProgress prog = getOrCreateClassProgress(player.getUniqueId(), activeClass.id());
            int refunded = 0;
            for (Map.Entry<String, Integer> entry : new HashMap<>(prog.getSkillLevelsMap()).entrySet()) {
                if (entry.getValue() > 1) {
                    refunded += (entry.getValue() - 1);
                    prog.setSkillLevel(entry.getKey(), 1);
                }
            }
            if (refunded <= 0) return false;
            profile.setCoins(profile.getCoins() - cost);
            prog.setFreeSkillPoints(prog.getFreeSkillPoints() + refunded);
            plugin.getCore().getDatabaseManager().runAsync(() -> plugin.getCore().getProfileManager().saveToDatabaseSync(profile));
            plugin.getCore().getDatabaseManager().runAsync(() -> savePlayerDataSync(player.getUniqueId()));
            plugin.getCore().getLang().send(player, "classes.respec-skills-done", LangManager.placeholders(
                    "class", activeClass.nameEs(),
                    "points", String.valueOf(refunded)
            ));
            return true;
        }
    }

    /**
     * Genera una pieza de armadura inteligente con Estadísticas Base Universales (para todas las clases)
     * y Pasiva/Bono Exclusivo que solo se activa si llevas la clase requerida.
     */
    public ItemStack createClassArmorPiece(RpgClassDefinition targetClass) {
        ItemStack armor = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = armor.getItemMeta();
        if (meta == null) return armor;

        double univHealth = 25.0;
        double univDef = 12.0;
        StatType classExclusiveStat = targetClass.id().contains("arcanista") || targetClass.id().contains("sylva")
                ? StatType.MAGIC_DAMAGE
                : StatType.PHYSICAL_DAMAGE;
        double classExclusiveBonus = 20.0;

        meta.setDisplayName(LangManager.colorize("&6&lCoraza Ascendente de " + targetClass.nameEs()));
        List<String> lore = new ArrayList<>();
        lore.add(LangManager.colorize("&8Armadura de Clase — The Last Ascension"));
        lore.add("");
        lore.add(LangManager.colorize("&7Estadísticas Base &a(Universales - Todas las Clases)&7:"));
        lore.add(LangManager.colorize(" &a+25.0 Vida Máxima"));
        lore.add(LangManager.colorize(" &a+12.0 Defensa Física"));
        lore.add("");
        lore.add(LangManager.colorize("&6Pasiva y Bono Exclusivo &e(Requiere Clase: " + targetClass.nameEs() + ")&6:"));
        lore.add(LangManager.colorize(" &b+" + classExclusiveBonus + " " + classExclusiveStat.getDisplayNameEs()));
        lore.add(LangManager.colorize(" &d★ Bonus de Set: Resonancia Elemental de " + targetClass.primaryElement().name()));
        meta.setLore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyUnivHealth, PersistentDataType.DOUBLE, univHealth);
        pdc.set(keyUnivDefense, PersistentDataType.DOUBLE, univDef);
        pdc.set(keyReqClass, PersistentDataType.STRING, targetClass.id());
        pdc.set(keyClassStat, PersistentDataType.STRING, classExclusiveStat.name());
        pdc.set(keyClassBonus, PersistentDataType.DOUBLE, classExclusiveBonus);

        armor.setItemMeta(meta);
        return armor;
    }

    /**
     * Recalcula todas las estadísticas del jugador combinando:
     * 1. Base por defecto de StatType
     * 2. Bonos de su Clase Activa
     * 3. Talismanes equipados (estilo Elden Ring)
     * 4. Armaduras equipadas (Stats Universales siempre + Stats de Clase solo si coincide su clase activa)
     * (Los puntos globales invertidos se suman automáticamente en PlayerProfile.getStat()).
     */
    public void recalculateAndApplyStats(Player player) {
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return;

        Map<StatType, Double> computedBase = new HashMap<>();
        for (StatType stat : StatType.values()) {
            computedBase.put(stat, stat.getDefaultValue());
        }

        RpgClassDefinition activeClass = plugin.getRegistry().getClass(profile.getActiveClassId());
        if (activeClass != null) {
            for (Map.Entry<StatType, Double> entry : activeClass.baseClassBonuses().entrySet()) {
                computedBase.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        // Sumar Talismanes equipados
        for (String talId : getEquippedTalismans(player.getUniqueId())) {
            TalismanDefinition tal = plugin.getRegistry().getTalisman(talId);
            if (tal != null) {
                for (Map.Entry<StatType, Double> entry : tal.statBonuses().entrySet()) {
                    computedBase.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
            }
        }

        // Sumar Armaduras equipadas (Universales siempre + Exclusivas de Clase si coincide)
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType() == Material.AIR) continue;
            ItemMeta meta = armor.getItemMeta();
            if (meta == null) continue;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            Double uHp = pdc.get(keyUnivHealth, PersistentDataType.DOUBLE);
            if (uHp != null) computedBase.merge(StatType.MAX_HEALTH, uHp, Double::sum);

            Double uDef = pdc.get(keyUnivDefense, PersistentDataType.DOUBLE);
            if (uDef != null) computedBase.merge(StatType.DEFENSE, uDef, Double::sum);

            String reqClass = pdc.get(keyReqClass, PersistentDataType.STRING);
            String cStatStr = pdc.get(keyClassStat, PersistentDataType.STRING);
            Double cBonus = pdc.get(keyClassBonus, PersistentDataType.DOUBLE);

            if (reqClass != null && cStatStr != null && cBonus != null) {
                boolean classMatches = activeClass != null && activeClass.matchesOrEvolvesFrom(reqClass);
                if (classMatches) {
                    try {
                        StatType cStat = StatType.valueOf(cStatStr);
                        computedBase.merge(cStat, cBonus, Double::sum);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        for (Map.Entry<StatType, Double> entry : computedBase.entrySet()) {
            profile.setBaseStat(entry.getKey(), entry.getValue());
        }

        // Reflejar Vida Máxima RPG en la barra de corazones de Minecraft (20 HP base = 100 Vida RPG -> escala suave)
        AttributeInstance maxHpAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHpAttr != null) {
            double rpgMaxHp = profile.getStat(StatType.MAX_HEALTH);
            double mcHealth = Math.max(20.0, Math.min(80.0, 20.0 + ((rpgMaxHp - 100.0) * 0.20)));
            maxHpAttr.setBaseValue(mcHealth);
            if (player.getHealth() > mcHealth) {
                player.setHealth(mcHealth);
            }
        }
    }

    private void spawnSkillEffect(Location loc, ElementType elem, int level, double radius) {
        if (loc.getWorld() == null) return;
        Particle p = elem == null ? Particle.SWEEP_ATTACK : switch (elem) {
            case IGNEO -> Particle.FLAME;
            case SYLVA -> Particle.SPORE_BLOSSOM_AIR;
            case TERRA -> Particle.CRIT;
            case FULMEN -> Particle.ELECTRIC_SPARK;
            case CRIO -> Particle.SNOWFLAKE;
            case CEFIRO -> Particle.CLOUD;
            case UMBRA -> Particle.SOUL_FIRE_FLAME;
            case ASTRAL -> Particle.END_ROD;
        };
        loc.getWorld().spawnParticle(p, loc.add(0, 1.0, 0), 20 + (level * 12), radius * 0.35, 0.5, radius * 0.35, 0.08);
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.9f + (level * 0.1f));
    }

    private AscendentCombat getCombatPlugin() {
        Plugin p = Bukkit.getPluginManager().getPlugin("AscendentCombat");
        return (p instanceof AscendentCombat combat && p.isEnabled()) ? combat : null;
    }

    private String serializeSkillLevels(PlayerClassProgress prog) {
        return prog.getSkillLevelsMap().entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }

    private void deserializeSkillLevels(String raw, PlayerClassProgress prog) {
        if (raw == null || raw.isBlank()) return;
        for (String pair : raw.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                try {
                    prog.setSkillLevel(kv[0], Integer.parseInt(kv[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private String serializeUnlockedSlots(PlayerClassProgress prog) {
        return prog.getUnlockedCategorySlotsMap().entrySet().stream()
                .map(e -> e.getKey().name() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }

    private void deserializeUnlockedSlots(String raw, PlayerClassProgress prog) {
        if (raw == null || raw.isBlank()) return;
        for (String pair : raw.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                try {
                    prog.setUnlockedSlots(SkillCategory.valueOf(kv[0]), Integer.parseInt(kv[1]));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}
