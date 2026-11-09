package com.thelastascension.ascendentClasses.model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Progreso independiente de un jugador dentro de una clase específica (estilo Trove).
 * Se guarda por separado para cada clase, de modo que al cambiar de clase gratis
 * nunca se pierde el nivel ni las mejoras de habilidades de las clases anteriores.
 */
public class PlayerClassProgress {

    private final String classId;
    private int classLevel = 1;
    private double classExp = 0.0;
    private int freeSkillPoints = 2;
    private final Map<String, Integer> skillLevels = new HashMap<>();
    private final Map<SkillCategory, Integer> unlockedCategorySlots = new EnumMap<>(SkillCategory.class);

    public PlayerClassProgress(String classId) {
        this.classId = classId.toLowerCase();
        for (SkillCategory cat : SkillCategory.values()) {
            unlockedCategorySlots.put(cat, 1);
        }
    }

    public String getClassId() {
        return classId;
    }

    public int getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(int classLevel) {
        this.classLevel = Math.max(1, classLevel);
    }

    public double getClassExp() {
        return classExp;
    }

    public void setClassExp(double classExp) {
        this.classExp = Math.max(0.0, classExp);
    }

    public int getFreeSkillPoints() {
        return freeSkillPoints;
    }

    public void setFreeSkillPoints(int freeSkillPoints) {
        this.freeSkillPoints = Math.max(0, freeSkillPoints);
    }

    public int getSkillLevel(String skillId) {
        return skillLevels.getOrDefault(skillId.toLowerCase(), 1);
    }

    public void setSkillLevel(String skillId, int level) {
        skillLevels.put(skillId.toLowerCase(), Math.max(1, level));
    }

    public Map<String, Integer> getSkillLevelsMap() {
        return skillLevels;
    }

    public int getUnlockedSlots(SkillCategory category) {
        return unlockedCategorySlots.getOrDefault(category, 1);
    }

    public void setUnlockedSlots(SkillCategory category, int slots) {
        unlockedCategorySlots.put(category, Math.max(1, slots));
    }

    public Map<SkillCategory, Integer> getUnlockedCategorySlotsMap() {
        return unlockedCategorySlots;
    }
}
