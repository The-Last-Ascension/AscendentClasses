package com.thelastascension.ascendentClasses.model;

import com.thelastascension.ascendentCore.model.ElementType;
import com.thelastascension.ascendentCore.model.StatType;
import org.bukkit.Material;

import java.util.Map;

/**
 * Define un Talismán equipable estilo Elden Ring.
 */
public record TalismanDefinition(
        String id,
        String nameEs,
        Material icon,
        Map<StatType, Double> statBonuses,
        ElementType boostedElement,
        double elementalDamageBonus,
        String descriptionEs
) {}
