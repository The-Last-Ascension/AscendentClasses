package com.thelastascension.ascendentClasses.model;

/**
 * Categorías de habilidades por clase.
 * Por defecto el jugador empieza con 1 ranura de cada categoría y puede desbloquear más (ej. 2 Definitivas).
 */
public enum SkillCategory {
    RAPIDA("Habilidad Rápida", "&a"),
    CARGADA("Habilidad Cargada", "&e"),
    MOVIMIENTO("Habilidad de Movimiento", "&b"),
    ESPECIAL("Habilidad Especial", "&d"),
    DEFINITIVA("Habilidad Definitiva", "&6&l");

    private final String displayNameEs;
    private final String colorPrefix;

    SkillCategory(String displayNameEs, String colorPrefix) {
        this.displayNameEs = displayNameEs;
        this.colorPrefix = colorPrefix;
    }

    public String getDisplayNameEs() {
        return displayNameEs;
    }

    public String getColorPrefix() {
        return colorPrefix;
    }
}
