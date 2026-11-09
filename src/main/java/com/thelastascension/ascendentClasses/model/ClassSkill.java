package com.thelastascension.ascendentClasses.model;

import com.thelastascension.ascendentCore.model.ElementType;
import org.bukkit.Material;

/**
 * Define una habilidad de clase mejorable por niveles (1..5).
 * Al subir de nivel aumenta su daño, su rango/tamaño AoE, su daño de Stun y sus efectos visuales.
 */
public record ClassSkill(
        String id,
        String nameEs,
        SkillCategory category,
        ElementType element,
        double baseDamage,
        double baseStun,
        double baseAoeRadius,
        int cooldownSeconds,
        Material icon,
        String descriptionEs
) {
    public double getScaledDamage(int level, double growthPerLevel) {
        int effectiveLevel = Math.max(1, level);
        return baseDamage * (1.0 + ((effectiveLevel - 1) * growthPerLevel));
    }

    public double getScaledStun(int level) {
        int effectiveLevel = Math.max(1, level);
        return baseStun * (1.0 + ((effectiveLevel - 1) * 0.20));
    }

    public double getScaledAoeRadius(int level, double aoeGrowthPerLevel) {
        int effectiveLevel = Math.max(1, level);
        return baseAoeRadius + ((effectiveLevel - 1) * aoeGrowthPerLevel);
    }
}
