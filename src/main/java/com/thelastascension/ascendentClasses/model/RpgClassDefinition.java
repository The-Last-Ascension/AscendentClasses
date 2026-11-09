package com.thelastascension.ascendentClasses.model;

import com.thelastascension.ascendentCore.model.ElementType;
import com.thelastascension.ascendentCore.model.StatType;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * Define una Clase RPG (Inicial tipo Starter de Pokémon o Evolución/Ascensión avanzada).
 */
public record RpgClassDefinition(
        String id,
        String nameEs,
        String roleEs,
        String parentStarterId,
        ElementType primaryElement,
        ElementType secondaryElement,
        Material icon,
        Map<StatType, Double> baseClassBonuses,
        List<String> evolutionIds,
        List<ClassSkill> skills,
        List<String> loreEs
) {
    public boolean isStarter() {
        return parentStarterId == null || parentStarterId.isBlank();
    }

    public boolean matchesOrEvolvesFrom(String requiredClassId) {
        if (requiredClassId == null || requiredClassId.isBlank()) return true;
        return this.id.equalsIgnoreCase(requiredClassId)
                || (this.parentStarterId != null && this.parentStarterId.equalsIgnoreCase(requiredClassId));
    }
}
