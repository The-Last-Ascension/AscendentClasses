package com.thelastascension.ascendentClasses.command;

import com.thelastascension.ascendentClasses.AscendentClasses;
import com.thelastascension.ascendentClasses.gui.ClassMenuGui;
import com.thelastascension.ascendentClasses.model.ClassSkill;
import com.thelastascension.ascendentClasses.model.PlayerClassProgress;
import com.thelastascension.ascendentClasses.model.RpgClassDefinition;
import com.thelastascension.ascendentClasses.model.TalismanDefinition;
import com.thelastascension.ascendentCore.i18n.LangManager;
import com.thelastascension.ascendentCore.model.PlayerProfile;
import com.thelastascension.ascendentCore.model.StatType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Comando /clase (/class, /talisman) para abrir el menú con pestañas o ejecutar acciones directas.
 */
public class ClassCommand implements CommandExecutor, TabCompleter {

    private final AscendentClasses plugin;

    public ClassCommand(AscendentClasses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getCore().getLang().send(sender, "system.player-only");
            return true;
        }

        if (args.length == 0) {
            ClassMenuGui.MenuTab defaultTab = label.equalsIgnoreCase("talisman")
                    ? ClassMenuGui.MenuTab.TALISMANS
                    : ClassMenuGui.MenuTab.CLASSES;
            plugin.getMenuGui().openMenu(player, defaultTab);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "menu" -> plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.CLASSES);
            case "stats" -> plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.STATS);
            case "skills", "habilidades" -> plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.SKILLS);
            case "talismanes", "talisman" -> plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.TALISMANS);
            case "elegir", "switch" -> {
                if (args.length < 2) {
                    plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.CLASSES);
                    return true;
                }
                plugin.getClassManager().switchClass(player, args[1]);
            }
            case "cast", "usar" -> {
                if (args.length < 2) {
                    plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.SKILLS);
                    return true;
                }
                handleCast(player, args[1]);
            }
            case "upgrade", "mejorar" -> {
                if (args.length < 2) {
                    plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.SKILLS);
                    return true;
                }
                plugin.getClassManager().upgradeClassSkill(player, args[1]);
            }
            case "respec" -> {
                boolean isStats = args.length < 2 || !args[1].equalsIgnoreCase("skills");
                plugin.getClassManager().respecPoints(player, isStats);
            }
            case "armor", "armadura" -> {
                if (!player.hasPermission("ascendent.classes.admin")) {
                    plugin.getCore().getLang().send(player, "system.no-permission");
                    return true;
                }
                String classId = args.length >= 2 ? args[1] : "vanguardia";
                RpgClassDefinition def = plugin.getRegistry().getClass(classId);
                if (def == null) def = plugin.getRegistry().getClass("vanguardia");
                ItemStack piece = plugin.getClassManager().createClassArmorPiece(def);
                player.getInventory().addItem(piece);
                plugin.getCore().getLang().send(player, "classes.armor-given", LangManager.placeholders("item", def.nameEs()));
            }
            case "admin" -> handleAdmin(player, args);
            default -> plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.CLASSES);
        }
        return true;
    }

    private void handleCast(Player player, String query) {
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null) return;
        RpgClassDefinition activeClass = plugin.getRegistry().getClass(profile.getActiveClassId());
        if (activeClass == null) {
            plugin.getMenuGui().openMenu(player, ClassMenuGui.MenuTab.CLASSES);
            return;
        }
        for (ClassSkill sk : activeClass.skills()) {
            if (sk.id().equalsIgnoreCase(query) || sk.category().name().equalsIgnoreCase(query)) {
                plugin.getClassManager().castSkill(player, sk);
                return;
            }
        }
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("ascendent.classes.admin")) {
            plugin.getCore().getLang().send(player, "system.no-permission");
            return;
        }
        PlayerProfile profile = plugin.getCore().getProfileManager().getCachedProfile(player.getUniqueId());
        if (profile == null || args.length < 2) return;

        String action = args[1].toLowerCase(Locale.ROOT);
        int amount = args.length >= 3 ? parseIntSafe(args[2], 5) : 5;

        switch (action) {
            case "addstatpts" -> {
                profile.setFreeStatPoints(profile.getFreeStatPoints() + amount);
                plugin.getCore().getDatabaseManager().runAsync(() -> plugin.getCore().getProfileManager().saveToDatabaseSync(profile));
                player.sendMessage(LangManager.colorize("&a[Admin] Añadidos +" + amount + " Puntos Globales de Estadística."));
            }
            case "addskillpts" -> {
                PlayerClassProgress prog = plugin.getClassManager().getOrCreateClassProgress(player.getUniqueId(), profile.getActiveClassId());
                prog.setFreeSkillPoints(prog.getFreeSkillPoints() + amount);
                player.sendMessage(LangManager.colorize("&a[Admin] Añadidos +" + amount + " Puntos de Habilidad a tu clase actual."));
            }
            case "setclasslvl" -> {
                PlayerClassProgress prog = plugin.getClassManager().getOrCreateClassProgress(player.getUniqueId(), profile.getActiveClassId());
                prog.setClassLevel(amount);
                player.sendMessage(LangManager.colorize("&a[Admin] Nivel de tu clase actual establecido en Nv. " + amount + "."));
            }
            case "addtalismanslot" -> {
                int nextSlots = Math.min(4, profile.getUnlockedTalismanSlots() + 1);
                profile.setUnlockedTalismanSlots(nextSlots);
                plugin.getCore().getDatabaseManager().runAsync(() -> plugin.getCore().getProfileManager().saveToDatabaseSync(profile));
                plugin.getCore().getLang().send(player, "classes.talisman-slot-unlocked", LangManager.placeholders("slots", String.valueOf(nextSlots)));
            }
        }
    }

    private int parseIntSafe(String val, int fallback) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("menu", "elegir", "stats", "skills", "talismanes", "cast", "upgrade", "respec", "armor", "admin"), args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "elegir", "switch", "armor", "armadura" ->
                        filter(plugin.getRegistry().getAllClasses().stream().map(RpgClassDefinition::id).toList(), args[1]);
                case "cast", "usar" -> filter(List.of("rapida", "cargada", "movimiento", "especial", "definitiva"), args[1]);
                case "respec" -> filter(List.of("stats", "skills"), args[1]);
                case "admin" -> filter(List.of("addstatpts", "addskillpts", "setclasslvl", "addtalismanslot"), args[1]);
                default -> List.of();
            };
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.startsWith(lower)).toList();
    }
}
