package com.thelastascension.ascendentClasses.gui;

import com.thelastascension.ascendentClasses.AscendentClasses;
import com.thelastascension.ascendentClasses.model.ClassSkill;
import com.thelastascension.ascendentClasses.model.PlayerClassProgress;
import com.thelastascension.ascendentClasses.model.RpgClassDefinition;
import com.thelastascension.ascendentClasses.model.TalismanDefinition;
import com.thelastascension.ascendentCore.i18n.LangManager;
import com.thelastascension.ascendentCore.model.PlayerProfile;
import com.thelastascension.ascendentCore.model.StatType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menú Interactivo con Pestañas (Tabs) para gestionar:
 * - Pestaña 1: Clases Iniciales (Starters) y Árbol de Ascensiones/Evoluciones (estilo Pokémon + Trove).
 * - Pestaña 2: Puntos de Estadística Globales (persisten al cambiar de clase) + Respec.
 * - Pestaña 3: Habilidades de Clase (Rápida, Cargada, Movimiento, Especial, Definitiva) + Mejoras de Nivel.
 * - Pestaña 4: Bolsa de Talismanes estilo Elden Ring.
 */
public class ClassMenuGui implements Listener {

    public enum MenuTab { CLASSES, STATS, SKILLS, TALISMANS }

    public record AscendentMenuHolder(MenuTab tab) implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return Bukkit.createInventory(this, 54);
        }
    }

    private final AscendentClasses plugin;
    private final NamespacedKey keyAction;
    private final NamespacedKey keyTarget;

    public ClassMenuGui(AscendentClasses plugin) {
        this.plugin = plugin;
        this.keyAction = new NamespacedKey(plugin, "gui_action");
        this.keyTarget = new NamespacedKey(plugin, "gui_target");
    }

    public void openMenu(Player player, MenuTab tab) {
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return;

        String lang = profile.getLanguage();
        String titleKey = switch (tab) {
            case CLASSES -> "classes.menu-title-classes";
            case STATS -> "classes.menu-title-stats";
            case SKILLS -> "classes.menu-title-skills";
            case TALISMANS -> "classes.menu-title-talismans";
        };
        String title = plugin.getCore().getLang().format(lang, titleKey, null);
        Inventory inv = Bukkit.createInventory(new AscendentMenuHolder(tab), 54, title);

        renderTopNavigationTabs(inv, tab, profile);

        switch (tab) {
            case CLASSES -> renderClassesTab(inv, player, profile);
            case STATS -> renderStatsTab(inv, profile);
            case SKILLS -> renderSkillsTab(inv, player, profile);
            case TALISMANS -> renderTalismansTab(inv, player, profile);
        }

        player.openInventory(inv);
    }

    private void renderTopNavigationTabs(Inventory inv, MenuTab activeTab, PlayerProfile profile) {
        ItemStack glass = createDecor(Material.GRAY_STAINED_GLASS_PANE, "&8");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
        }

        inv.setItem(1, createButton(
                activeTab == MenuTab.CLASSES ? Material.NETHER_STAR : Material.BOOK,
                "&6&l[1] Clases y Ascensiones",
                List.of("&7Previsualiza todas las clases iniciales", "&7y sus evoluciones estilo Pokémon/Trove.", "", "&e► Clic para abrir pestaña"),
                "tab", "CLASSES"
        ));
        inv.setItem(3, createButton(
                activeTab == MenuTab.STATS ? Material.NETHER_STAR : Material.EXPERIENCE_BOTTLE,
                "&a&l[2] Estadísticas Globales (&e" + profile.getFreeStatPoints() + " Pts&a&l)",
                List.of("&7Tus puntos globales persisten siempre", "&7aunque cambies de clase.", "", "&e► Clic para abrir pestaña"),
                "tab", "STATS"
        ));
        inv.setItem(5, createButton(
                activeTab == MenuTab.SKILLS ? Material.NETHER_STAR : Material.BLAZE_POWDER,
                "&b&l[3] Habilidades de Clase",
                List.of("&7Mejora el daño, rango AoE y efectos", "&7de las habilidades de tu clase activa.", "", "&e► Clic para abrir pestaña"),
                "tab", "SKILLS"
        ));
        inv.setItem(7, createButton(
                activeTab == MenuTab.TALISMANS ? Material.NETHER_STAR : Material.AMETHYST_SHARD,
                "&d&l[4] Bolsa de Talismanes (&f" + profile.getUnlockedTalismanSlots() + " Huecos&d&l)",
                List.of("&7Equipa Talismanes estilo Elden Ring", "&7para obtener pasivas y atributos.", "", "&e► Clic para abrir pestaña"),
                "tab", "TALISMANS"
        ));
    }

    private void renderClassesTab(Inventory inv, Player player, PlayerProfile profile) {
        String[] starters = {"vanguardia", "filo_sombra", "arcanista", "custodio_sylva"};
        int[] starterColumns = {1, 3, 5, 7};

        for (int idx = 0; idx < starters.length; idx++) {
            RpgClassDefinition starter = plugin.getRegistry().getClass(starters[idx]);
            if (starter == null) continue;
            int col = starterColumns[idx];

            // Fila 2 (slots 18 + col): Clase Starter
            inv.setItem(18 + col, buildClassItem(player, profile, starter));

            // Filas 3 y 4 (slots 27 + col y 36 + col): Sus 2 Ramas de Ascensión/Evolución
            List<String> evos = starter.evolutionIds();
            for (int e = 0; e < evos.size() && e < 2; e++) {
                RpgClassDefinition evoDef = plugin.getRegistry().getClass(evos.get(e));
                if (evoDef != null) {
                    inv.setItem(27 + (e * 9) + col, buildClassItem(player, profile, evoDef));
                }
            }
        }
    }

    private ItemStack buildClassItem(Player player, PlayerProfile profile, RpgClassDefinition def) {
        PlayerClassProgress prog = plugin.getClassManager().getOrCreateClassProgress(player.getUniqueId(), def.id());
        boolean isActive = def.id().equalsIgnoreCase(profile.getActiveClassId());

        List<String> lore = new ArrayList<>();
        lore.add("&8" + (def.isStarter() ? "Clase Inicial (Starter)" : "Ascensión de " + def.parentStarterId().toUpperCase()));
        lore.add("&7Rol: &f" + def.roleEs());
        lore.add("&7Elementos: &e" + def.primaryElement().name() + " &7/ &b" + def.secondaryElement().name());
        lore.add("&7Tu Progreso Guardado: &6Nv. " + prog.getClassLevel() + " &8(&b" + prog.getFreeSkillPoints() + " Pts. Habilidad&8)");
        lore.add("");
        lore.add("&6Bonificaciones Base de la Clase:");
        for (Map.Entry<StatType, Double> entry : def.baseClassBonuses().entrySet()) {
            lore.add(" &a+" + entry.getValue() + " &f" + entry.getKey().getDisplayNameEs());
        }
        lore.add("");
        lore.add("&bÁrbol de Habilidades (" + def.skills().size() + "):");
        for (ClassSkill sk : def.skills()) {
            lore.add(" &8- " + sk.category().getColorPrefix() + "[" + sk.category().name() + "] &f" + sk.nameEs());
        }
        lore.add("");
        if (isActive) {
            lore.add("&a&l✔ CLASE ACTIVA ACTUALMENTE");
        } else {
            lore.add("&e► Clic Izquierdo para cambiar gratis a esta clase");
        }

        return createButton(
                def.icon(),
                (isActive ? "&a&l★ " : "&6&l") + def.nameEs(),
                lore,
                "switch_class",
                def.id()
        );
    }

    private void renderStatsTab(Inventory inv, PlayerProfile profile) {
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 29, 31, 33};
        StatType[] stats = StatType.values();

        for (int i = 0; i < stats.length && i < slots.length; i++) {
            StatType stat = stats[i];
            int invested = profile.getInvestedPoints(stat);
            double total = profile.getStat(stat);

            List<String> lore = List.of(
                    "&7Valor Total Actual: &a" + String.format("%.1f", total),
                    "&7Puntos Globales Invertidos: &e" + invested,
                    "&7Puntos Globales Libres: &b" + profile.getFreeStatPoints(),
                    "",
                    "&8Estos puntos pertenecen a tu jugador y",
                    "&8se mantienen siempre al cambiar de clase.",
                    "",
                    "&e► Clic Izquierdo: Invertir +1 Punto",
                    "&6► Shift + Clic: Invertir +5 Puntos"
            );
            inv.setItem(slots[i], createButton(
                    Material.NETHER_STAR,
                    "&e&l" + stat.getDisplayNameEs(),
                    lore,
                    "invest_stat",
                    stat.name()
            ));
        }

        long cost = plugin.getConfig().getLong("classes.respec-coin-cost", 250L);
        inv.setItem(49, createButton(
                Material.HEART_OF_THE_SEA,
                "&c&lResetear Estadísticas Globales (Respec)",
                List.of(
                        "&7Recupera todos tus Puntos Globales invertidos",
                        "&7para redistribuirlos libremente.",
                        "&7Coste: &6" + cost + " Monedas",
                        "",
                        "&c► Clic para resetear puntos globales"
                ),
                "respec", "stats"
        ));
    }

    private void renderSkillsTab(Inventory inv, Player player, PlayerProfile profile) {
        RpgClassDefinition activeClass = plugin.getRegistry().getClass(profile.getActiveClassId());
        if (activeClass == null) {
            inv.setItem(22, createButton(
                    Material.BARRIER,
                    "&cNo has elegido ninguna clase aún",
                    List.of("&7Ve a la Pestaña [1] para elegir tu clase inicial."),
                    "tab", "CLASSES"
            ));
            return;
        }

        PlayerClassProgress prog = plugin.getClassManager().getOrCreateClassProgress(player.getUniqueId(), activeClass.id());
        int maxLevel = plugin.getConfig().getInt("skills.max-skill-level", 5);
        double dmgGrowth = plugin.getConfig().getDouble("skills.damage-growth-per-level", 0.25);
        double aoeGrowth = plugin.getConfig().getDouble("skills.aoe-growth-per-level", 0.60);

        int[] skillSlots = {20, 21, 22, 23, 24};
        List<ClassSkill> skills = activeClass.skills();
        for (int i = 0; i < skills.size() && i < skillSlots.length; i++) {
            ClassSkill sk = skills.get(i);
            int lvl = prog.getSkillLevel(sk.id());
            int catSlots = prog.getUnlockedSlots(sk.category());

            List<String> lore = List.of(
                    "&8Categoría: " + sk.category().getColorPrefix() + sk.category().getDisplayNameEs() + " &8(Ranuras: " + catSlots + ")",
                    "&7Elemento: &e" + sk.element().name() + " &8| &7Cooldown: &f" + sk.cooldownSeconds() + "s",
                    "&7Nivel Actual: &6" + lvl + "/" + maxLevel,
                    "",
                    "&f" + sk.descriptionEs(),
                    "",
                    "&c✦ Daño Escalado: &f" + String.format("%.1f", sk.getScaledDamage(lvl, dmgGrowth)),
                    "&e✦ Stun Aplicado: &f+" + String.format("%.1f", sk.getScaledStun(lvl)),
                    "&b✦ Tamaño/Rango AoE: &f" + String.format("%.1f", sk.getScaledAoeRadius(lvl, aoeGrowth)) + " bloques",
                    "",
                    "&7Puntos de Habilidad de " + activeClass.nameEs() + ": &b" + prog.getFreeSkillPoints(),
                    "&a► Clic Izquierdo: ¡Lanzar Habilidad ahora!",
                    "&e► Clic Derecho: Mejorar Nivel (+Daño, +AoE, +Efectos)"
            );
            inv.setItem(skillSlots[i], createButton(
                    sk.icon(),
                    sk.category().getColorPrefix() + sk.nameEs() + " &7[Nv. " + lvl + "]",
                    lore,
                    "skill_interact",
                    sk.id()
            ));
        }

        long cost = plugin.getConfig().getLong("classes.respec-coin-cost", 250L);
        inv.setItem(49, createButton(
                Material.ECHO_SHARD,
                "&c&lResetear Puntos de Habilidad de " + activeClass.nameEs(),
                List.of(
                        "&7Recupera los puntos invertidos en esta clase.",
                        "&7Coste: &6" + cost + " Monedas",
                        "",
                        "&c► Clic para resetear habilidades de clase"
                ),
                "respec", "skills"
        ));
    }

    private void renderTalismansTab(Inventory inv, Player player, PlayerProfile profile) {
        List<String> equipped = plugin.getClassManager().getEquippedTalismans(player.getUniqueId());
        int maxSlots = profile.getUnlockedTalismanSlots();

        int[] slots = {20, 21, 22, 23, 24, 31};
        int i = 0;
        for (TalismanDefinition tal : plugin.getRegistry().getAllTalismans()) {
            if (i >= slots.length) break;
            boolean isEquipped = equipped.contains(tal.id().toLowerCase());

            List<String> lore = new ArrayList<>();
            lore.add("&8Talismán Ancestral — Estilo Elden Ring");
            lore.add("&f" + tal.descriptionEs());
            lore.add("");
            for (Map.Entry<StatType, Double> entry : tal.statBonuses().entrySet()) {
                lore.add(" &a+" + entry.getValue() + " " + entry.getKey().getDisplayNameEs());
            }
            lore.add("");
            lore.add("&7Ranuras de Talismán usadas: &d" + equipped.size() + "/" + maxSlots);
            lore.add(isEquipped ? "&a&l✔ EQUIPADO &7(Clic para quitar)" : "&e► Clic para equipar en tu Bolsa de Talismanes");

            inv.setItem(slots[i++], createButton(
                    tal.icon(),
                    (isEquipped ? "&a&l★ " : "&d&l") + tal.nameEs(),
                    lore,
                    "toggle_talisman",
                    tal.id()
            ));
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AscendentMenuHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String action = meta.getPersistentDataContainer().get(keyAction, PersistentDataType.STRING);
        String target = meta.getPersistentDataContainer().get(keyTarget, PersistentDataType.STRING);
        if (action == null || target == null) return;

        switch (action) {
            case "tab" -> openMenu(player, MenuTab.valueOf(target));
            case "switch_class" -> {
                plugin.getClassManager().switchClass(player, target);
                openMenu(player, MenuTab.CLASSES);
            }
            case "invest_stat" -> {
                int amount = event.isShiftClick() ? 5 : 1;
                plugin.getClassManager().investGlobalStatPoint(player, StatType.valueOf(target), amount);
                openMenu(player, MenuTab.STATS);
            }
            case "skill_interact" -> {
                if (event.isRightClick()) {
                    plugin.getClassManager().upgradeClassSkill(player, target);
                    openMenu(player, MenuTab.SKILLS);
                } else {
                    PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
                    if (profile != null) {
                        RpgClassDefinition cls = plugin.getRegistry().getClass(profile.getActiveClassId());
                        if (cls != null) {
                            cls.skills().stream().filter(s -> s.id().equalsIgnoreCase(target)).findFirst().ifPresent(sk -> {
                                player.closeInventory();
                                plugin.getClassManager().castSkill(player, sk);
                            });
                        }
                    }
                }
            }
            case "toggle_talisman" -> {
                plugin.getClassManager().toggleTalisman(player, target);
                openMenu(player, MenuTab.TALISMANS);
            }
            case "respec" -> {
                boolean isStats = target.equalsIgnoreCase("stats");
                plugin.getClassManager().respecPoints(player, isStats);
                openMenu(player, holder.tab());
            }
        }
    }

    private ItemStack createDecor(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LangManager.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createButton(Material mat, String name, List<String> loreLines, String action, String target) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LangManager.colorize(name));
            meta.setLore(loreLines.stream().map(LangManager::colorize).toList());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.getPersistentDataContainer().set(keyAction, PersistentDataType.STRING, action);
            meta.getPersistentDataContainer().set(keyTarget, PersistentDataType.STRING, target);
            item.setItemMeta(meta);
        }
        return item;
    }
}
