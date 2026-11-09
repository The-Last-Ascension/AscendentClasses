package com.thelastascension.ascendentClasses;

import com.thelastascension.ascendentClasses.command.ClassCommand;
import com.thelastascension.ascendentClasses.gui.ClassMenuGui;
import com.thelastascension.ascendentClasses.manager.ClassAndGearManager;
import com.thelastascension.ascendentClasses.registry.ClassAndTalismanRegistry;
import com.thelastascension.ascendentCore.AscendentCore;
import com.thelastascension.ascendentCore.module.AscendentModule;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AscendentClasses v1.0.0 — Módulo de Clases, Árbol de Habilidades, Armaduras de Clase y Talismanes
 * de The Last Ascension (Ascendant).
 */
public final class AscendentClasses extends JavaPlugin implements AscendentModule {

    private AscendentCore core;
    private ClassAndTalismanRegistry registry;
    private ClassAndGearManager classManager;
    private ClassMenuGui menuGui;

    @Override
    public void onEnable() {
        this.core = AscendentCore.getInstance();
        if (this.core == null) {
            getLogger().severe("No se encontró AscendentCore activo. Desactivando AscendentClasses.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        this.core.getLang().registerPluginLanguageFiles(this, "lang/es_es.yml", "lang/en_us.yml");

        this.registry = new ClassAndTalismanRegistry();
        this.classManager = new ClassAndGearManager(this);
        this.classManager.initializeDatabase();

        this.menuGui = new ClassMenuGui(this);

        getServer().getPluginManager().registerEvents(this.classManager, this);
        getServer().getPluginManager().registerEvents(this.menuGui, this);

        PluginCommand cmd = getCommand("clase");
        if (cmd != null) {
            ClassCommand executor = new ClassCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        this.core.getModuleRegistry().registerModule(this);
        getLogger().info("AscendentClasses v" + getVersion() + " activado (12 Clases, Habilidades Evolutivas, Armaduras y Talismanes listos).");
    }

    @Override
    public void onDisable() {
        if (this.core != null && this.core.getModuleRegistry() != null) {
            this.core.getModuleRegistry().unregisterModule(getModuleId());
        }
        if (this.classManager != null) {
            this.classManager.saveAllSync();
        }
    }

    @Override
    public String getModuleId() {
        return "AscendentClasses";
    }

    @Override
    public String getVersion() {
        return getPluginMeta().getVersion();
    }

    @Override
    public Plugin getPlugin() {
        return this;
    }

    @Override
    public void onReload() {
        reloadConfig();
        if (this.core != null) {
            this.core.getLang().registerPluginLanguageFiles(this, "lang/es_es.yml", "lang/en_us.yml");
        }
    }

    public AscendentCore getCore() {
        return core;
    }

    public ClassAndTalismanRegistry getRegistry() {
        return registry;
    }

    public ClassAndGearManager getClassManager() {
        return classManager;
    }

    public ClassMenuGui getMenuGui() {
        return menuGui;
    }
}
