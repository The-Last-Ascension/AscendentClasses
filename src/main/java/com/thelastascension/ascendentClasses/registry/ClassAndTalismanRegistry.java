package com.thelastascension.ascendentClasses.registry;

import com.thelastascension.ascendentClasses.model.ClassSkill;
import com.thelastascension.ascendentClasses.model.RpgClassDefinition;
import com.thelastascension.ascendentClasses.model.SkillCategory;
import com.thelastascension.ascendentClasses.model.TalismanDefinition;
import com.thelastascension.ascendentCore.model.ElementType;
import com.thelastascension.ascendentCore.model.StatType;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catálogo central de las Clases Iniciales (Starters estilo Pokémon), sus Ramas de Ascensión/Evolución
 * y los Talismanes estilo Elden Ring.
 */
public class ClassAndTalismanRegistry {

    private final Map<String, RpgClassDefinition> classes = new LinkedHashMap<>();
    private final Map<String, TalismanDefinition> talismans = new LinkedHashMap<>();

    public ClassAndTalismanRegistry() {
        registerClasses();
        registerTalismans();
    }

    private void registerClasses() {
        // ==================== 1. STARTER: VANGUARDIA & EVOLUCIONES ====================
        registerClass(new RpgClassDefinition(
                "vanguardia", "Vanguardia", "Tanque / Rompepostura (Parry & Stun)", null,
                ElementType.TERRA, ElementType.IGNEO, Material.SHIELD,
                Map.of(StatType.MAX_HEALTH, 40.0, StatType.DEFENSE, 14.0, StatType.MAX_STUN, 35.0, StatType.STUN_DAMAGE, 10.0),
                List.of("paladin_astral", "coloso_igneo"),
                List.of(
                        new ClassSkill("tajo_baluarte", "Tajo de Baluarte", SkillCategory.RAPIDA, ElementType.TERRA, 8.0, 18.0, 2.8, 3, Material.IRON_SWORD, "Golpe rápido con el canto del escudo que desestabiliza al rival."),
                        new ClassSkill("embate_sismico", "Embate Sísmico", SkillCategory.CARGADA, ElementType.TERRA, 15.0, 35.0, 4.0, 7, Material.ANVIL, "Golpe pesado contra el suelo que fractura la postura enemiga."),
                        new ClassSkill("carga_ inquebrantable".replace(" ", ""), "Carga Inquebrantable", SkillCategory.MOVIMIENTO, ElementType.IGNEO, 10.0, 20.0, 3.2, 8, Material.IRON_BOOTS, "Avance acorazado que empuja a los enemigos a tu paso."),
                        new ClassSkill("rugido_titan", "Rugido del Titán", SkillCategory.ESPECIAL, ElementType.TERRA, 12.0, 30.0, 5.0, 12, Material.GOAT_HORN, "Onda expansiva defensiva que aplica Cristalizado en área."),
                        new ClassSkill("juicio_tectonico", "Juicio Tectónico", SkillCategory.DEFINITIVA, ElementType.IGNEO, 28.0, 65.0, 7.0, 25, Material.NETHERITE_AXE, "Definitiva: Levanta pilares volcánicos que aturden e impregnan con Ígneo.")
                ),
                List.of("Especialista en Parry y control de Stun.", "Evoluciona al Nv. 10 en Paladín Astral o Coloso Ígneo.")
        ));

        registerClass(new RpgClassDefinition(
                "paladin_astral", "Paladín Astral", "Tanque Sagrado / Bastión Estelar", "vanguardia",
                ElementType.ASTRAL, ElementType.TERRA, Material.GOLDEN_CHESTPLATE,
                Map.of(StatType.MAX_HEALTH, 75.0, StatType.DEFENSE, 24.0, StatType.MAX_STUN, 60.0, StatType.MAGIC_DAMAGE, 16.0),
                List.of(),
                List.of(
                        new ClassSkill("corte_lumen", "Corte de Lumen", SkillCategory.RAPIDA, ElementType.ASTRAL, 12.0, 22.0, 3.0, 3, Material.GOLDEN_SWORD, "Tajo luminoso que aplica Marca Estelar."),
                        new ClassSkill("martillo_estelar", "Martillo de Gravedad", SkillCategory.CARGADA, ElementType.ASTRAL, 22.0, 45.0, 4.5, 7, Material.MACE, "Aplasta con gravedad cósmica y rompe guardias."),
                        new ClassSkill("salto_valquiria", "Salto de Valquiria", SkillCategory.MOVIMIENTO, ElementType.ASTRAL, 14.0, 25.0, 3.8, 8, Material.FEATHER, "Descenso estelar sobre el objetivo."),
                        new ClassSkill("egida_celeste", "Égida Celeste", SkillCategory.ESPECIAL, ElementType.TERRA, 18.0, 35.0, 5.5, 12, Material.TOTEM_OF_UNDYING, "Consagra el suelo reduciendo el Stun de los aliados."),
                        new ClassSkill("supernova_bastion", "Supernova del Bastión", SkillCategory.DEFINITIVA, ElementType.ASTRAL, 42.0, 85.0, 8.5, 25, Material.NETHER_STAR, "Definitiva: Explosión gravitatoria que colapsa a todos los enemigos.")
                ),
                List.of("Ascensión de Vanguardia imbuida en luz y gravedad Astral.")
        ));

        registerClass(new RpgClassDefinition(
                "coloso_igneo", "Coloso Ígneo", "Juggernaut Volcánico / Destructor de Posturas", "vanguardia",
                ElementType.IGNEO, ElementType.TERRA, Material.MAGMA_CREAM,
                Map.of(StatType.MAX_HEALTH, 65.0, StatType.DEFENSE, 20.0, StatType.PHYSICAL_DAMAGE, 22.0, StatType.STUN_DAMAGE, 22.0),
                List.of(),
                List.of(
                        new ClassSkill("puño_brasa", "Puño de Brasa", SkillCategory.RAPIDA, ElementType.IGNEO, 13.0, 24.0, 3.0, 3, Material.BLAZE_POWDER, "Golpe ígneo que quema la armadura rival."),
                        new ClassSkill("ruptura_magma", "Ruptura de Magma", SkillCategory.CARGADA, ElementType.IGNEO, 24.0, 50.0, 4.5, 7, Material.MAGMA_BLOCK, "Fisura volcánica de alto impacto."),
                        new ClassSkill("estampida_ceniza", "Estampida de Ceniza", SkillCategory.MOVIMIENTO, ElementType.IGNEO, 15.0, 30.0, 3.8, 8, Material.FIRE_CHARGE, "Embiste envuelto en llamas."),
                        new ClassSkill("coraza_fundida", "Coraza Fundida", SkillCategory.ESPECIAL, ElementType.TERRA, 18.0, 35.0, 5.0, 12, Material.LAVA_BUCKET, "Explosión térmica alrededor del coloso."),
                        new ClassSkill("cataclismo_volcanico", "Cataclismo Volcánico", SkillCategory.DEFINITIVA, ElementType.IGNEO, 45.0, 90.0, 8.5, 25, Material.NETHERITE_INGOT, "Definitiva: Erupción masiva que calcina y aturde toda la sala.")
                ),
                List.of("Ascensión de Vanguardia enfocada en daño explosivo y Stun masivo.")
        ));

        // ==================== 2. STARTER: FILO DE SOMBRA & EVOLUCIONES ====================
        registerClass(new RpgClassDefinition(
                "filo_sombra", "Filo de Sombra", "Asesino Crítico / Velocidad y Evasión", null,
                ElementType.UMBRA, ElementType.CEFIRO, Material.NETHERITE_SWORD,
                Map.of(StatType.PHYSICAL_DAMAGE, 14.0, StatType.CRIT_CHANCE, 15.0, StatType.CRIT_DAMAGE, 35.0, StatType.MOVE_SPEED, 0.20),
                List.of("monarca_umbra", "tempestad_cefiro"),
                List.of(
                        new ClassSkill("corte_crepuscular", "Corte Crepuscular", SkillCategory.RAPIDA, ElementType.UMBRA, 11.0, 12.0, 2.8, 2, Material.STONE_SWORD, "Doble tajo veloz que aplica Maldición de Alma."),
                        new ClassSkill("estocada_letal", "Estocada Perforante", SkillCategory.CARGADA, ElementType.UMBRA, 20.0, 22.0, 3.5, 6, Material.IRON_SWORD, "Golpe crítico concentrado que atraviesa defensas."),
                        new ClassSkill("paso_umbrio", "Paso Umbrío", SkillCategory.MOVIMIENTO, ElementType.CEFIRO, 9.0, 10.0, 3.5, 6, Material.ENDER_PEARL, "Desplazamiento relámpago dejando una estela cortante."),
                        new ClassSkill("danza_cuchillas", "Danza de Cuchillas", SkillCategory.ESPECIAL, ElementType.CEFIRO, 16.0, 18.0, 4.8, 10, Material.SHEARS, "Torbellino de cortes de viento y sombra."),
                        new ClassSkill("eclipse_carmesi", "Eclipse Carmesí", SkillCategory.DEFINITIVA, ElementType.UMBRA, 36.0, 45.0, 7.5, 22, Material.WITHER_SKELETON_SKULL, "Definitiva: Desata cortes abisales sobre todos los enemigos cercanos.")
                ),
                List.of("Maestro del daño crítico, la movilidad y las sombras.", "Evoluciona al Nv. 10 en Monarca de las Sombras o Danzante del Vendaval.")
        ));

        registerClass(new RpgClassDefinition(
                "monarca_umbra", "Monarca de las Sombras", "Segador Abisal / Dominio de Almas", "filo_sombra",
                ElementType.UMBRA, ElementType.ASTRAL, Material.ECHO_SHARD,
                Map.of(StatType.PHYSICAL_DAMAGE, 26.0, StatType.MAGIC_DAMAGE, 18.0, StatType.CRIT_CHANCE, 25.0, StatType.CRIT_DAMAGE, 60.0),
                List.of(),
                List.of(
                        new ClassSkill("garra_abismo", "Garra del Abismo", SkillCategory.RAPIDA, ElementType.UMBRA, 16.0, 16.0, 3.2, 2, Material.ECHO_SHARD, "Desgarra el alma del objetivo."),
                        new ClassSkill("guadaña_monarca", "Guadaña del Monarca", SkillCategory.CARGADA, ElementType.UMBRA, 28.0, 30.0, 4.5, 6, Material.NETHERITE_HOE, "Corte en media luna de energía oscura."),
                        new ClassSkill("transferencia_sombra", "Transferencia de Sombra", SkillCategory.MOVIMIENTO, ElementType.UMBRA, 14.0, 15.0, 4.0, 6, Material.ENDER_EYE, "Cruza el plano de las sombras dañando al salir."),
                        new ClassSkill("legion_nocturna", "Legión Nocturna", SkillCategory.ESPECIAL, ElementType.UMBRA, 24.0, 25.0, 5.5, 11, Material.SOUL_LANTERN, "Invoca espectros que acribillan el área."),
                        new ClassSkill("dominio_monarca", "Dominio del Monarca Abisal", SkillCategory.DEFINITIVA, ElementType.UMBRA, 50.0, 60.0, 8.5, 24, Material.DRAGON_BREATH, "Definitiva: Consagra un territorio de oscuridad absoluta.")
                ),
                List.of("Ascensión de Filo de Sombra inspirada en el poder de las sombras.")
        ));

        registerClass(new RpgClassDefinition(
                "tempestad_cefiro", "Danzante del Vendaval", "Espadachín Aéreo / Velocidad Extrema", "filo_sombra",
                ElementType.CEFIRO, ElementType.FULMEN, Material.BREEZE_ROD,
                Map.of(StatType.PHYSICAL_DAMAGE, 24.0, StatType.ATTACK_SPEED, 0.45, StatType.MOVE_SPEED, 0.35, StatType.CRIT_CHANCE, 22.0),
                List.of(),
                List.of(
                        new ClassSkill("estocada_cefiro", "Estocada de Céfiro", SkillCategory.RAPIDA, ElementType.CEFIRO, 15.0, 15.0, 3.2, 2, Material.BREEZE_ROD, "Ráfaga de estocadas supersónicas."),
                        new ClassSkill("corte_vacio_aereo", "Corte de Vacío Aéreo", SkillCategory.CARGADA, ElementType.CEFIRO, 26.0, 28.0, 4.5, 6, Material.WIND_CHARGE, "Onda de presión que corta a distancia."),
                        new ClassSkill("impulso_relampago", "Impulso Relámpago", SkillCategory.MOVIMIENTO, ElementType.FULMEN, 14.0, 18.0, 4.2, 5, Material.SUGAR, "Aceleración eléctrica instantánea."),
                        new ClassSkill("voragine_cortante", "Vorágine Cortante", SkillCategory.ESPECIAL, ElementType.CEFIRO, 22.0, 25.0, 5.8, 10, Material.COBWEB, "Ciclón que atrae y lacera a los enemigos."),
                        new ClassSkill("huracan_mil_hojas", "Huracán de las Mil Hojas", SkillCategory.DEFINITIVA, ElementType.CEFIRO, 46.0, 55.0, 8.5, 22, Material.TRIDENT, "Definitiva: Tormenta de espadas de viento y trueno.")
                ),
                List.of("Ascensión de Filo de Sombra que domina el viento y el relámpago.")
        ));

        // ==================== 3. STARTER: ARCANISTA ELEMENTAL & EVOLUCIONES ====================
        registerClass(new RpgClassDefinition(
                "arcanista", "Arcanista Elemental", "Mago de Reacciones y Control en Área", null,
                ElementType.FULMEN, ElementType.CRIO, Material.BLAZE_ROD,
                Map.of(StatType.MAGIC_DAMAGE, 20.0, StatType.CRIT_CHANCE, 10.0, StatType.CRIT_DAMAGE, 25.0),
                List.of("sabio_abismo", "soberano_fulmen"),
                List.of(
                        new ClassSkill("chispa_voltaica", "Chispa Voltaica", SkillCategory.RAPIDA, ElementType.FULMEN, 11.0, 14.0, 3.5, 2, Material.AMETHYST_SHARD, "Descarga mágica que impregna con Fulmen."),
                        new ClassSkill("lanza_glaciar", "Lanza Glaciar", SkillCategory.CARGADA, ElementType.CRIO, 19.0, 26.0, 4.5, 6, Material.BLUE_ICE, "Proyectil pesado de hielo que impregna con Crio y detona Superconductividad."),
                        new ClassSkill("destello_astral", "Destello Arcano", SkillCategory.MOVIMIENTO, ElementType.FULMEN, 10.0, 12.0, 3.5, 7, Material.GLOWSTONE_DUST, "Teletransporte corto que deja una descarga eléctrica."),
                        new ClassSkill("convergencia_runica", "Convergencia Rúnica", SkillCategory.ESPECIAL, ElementType.CRIO, 18.0, 24.0, 5.5, 11, Material.PRISMARINE_CRYSTALS, "Anillo de escarcha que congela la postura enemiga."),
                        new ClassSkill("tormenta_primigenia", "Tormenta Primigenia", SkillCategory.DEFINITIVA, ElementType.FULMEN, 38.0, 50.0, 8.0, 24, Material.LIGHTNING_ROD, "Definitiva: Lluvia de relámpagos y escarcha sobre todo el campo.")
                ),
                List.of("Capaz de encadenar reacciones elementales por sí mismo o en grupo.", "Evoluciona al Nv. 10 en Sabio del Cero Absoluto o Soberano de la Tormenta.")
        ));

        registerClass(new RpgClassDefinition(
                "sabio_abismo", "Sabio del Cero Absoluto", "Archimago Crio-Astral / Control Total", "arcanista",
                ElementType.CRIO, ElementType.ASTRAL, Material.PACKED_ICE,
                Map.of(StatType.MAGIC_DAMAGE, 38.0, StatType.MAX_HEALTH, 25.0, StatType.STUN_DAMAGE, 18.0),
                List.of(),
                List.of(
                        new ClassSkill("fragmento_cero", "Fragmento Cero", SkillCategory.RAPIDA, ElementType.CRIO, 15.0, 18.0, 3.8, 2, Material.SNOWBALL, "Cristales de hielo absoluto."),
                        new ClassSkill("cometa_helado", "Cometa Helado", SkillCategory.CARGADA, ElementType.ASTRAL, 27.0, 35.0, 5.0, 6, Material.SEA_LANTERN, "Impacto astral que detona reacciones sobre enemigos escarchados."),
                        new ClassSkill("paso_espejismo", "Paso de Espejismo", SkillCategory.MOVIMIENTO, ElementType.CRIO, 13.0, 16.0, 4.0, 6, Material.GLASS, "Deslizamiento cristalino."),
                        new ClassSkill("prision_invernal", "Prisión Invernal", SkillCategory.ESPECIAL, ElementType.CRIO, 24.0, 32.0, 6.0, 11, Material.POWDER_SNOW_BUCKET, "Congela y reduce la velocidad en gran área."),
                        new ClassSkill("aurora_fin_mundo", "Aurora del Fin del Mundo", SkillCategory.DEFINITIVA, ElementType.CRIO, 48.0, 70.0, 9.0, 24, Material.BEACON, "Definitiva: Detiene el calor vital en toda la sala.")
                ),
                List.of("Ascensión de Arcanista maestra del hielo absoluto y el cosmos.")
        ));

        registerClass(new RpgClassDefinition(
                "soberano_fulmen", "Soberano de la Tormenta", "Artillero de Plasma y Fuego", "arcanista",
                ElementType.FULMEN, ElementType.IGNEO, Material.END_CRYSTAL,
                Map.of(StatType.MAGIC_DAMAGE, 42.0, StatType.CRIT_CHANCE, 18.0, StatType.CRIT_DAMAGE, 45.0),
                List.of(),
                List.of(
                        new ClassSkill("arco_plasma", "Arco de Plasma", SkillCategory.RAPIDA, ElementType.FULMEN, 16.0, 16.0, 3.8, 2, Material.REDSTONE, "Rayo en cadena de alta frecuencia."),
                        new ClassSkill("cañon_rail", "Cañón Electromagnético", SkillCategory.CARGADA, ElementType.FULMEN, 30.0, 32.0, 5.0, 6, Material.COMPARATOR, "Disparo sobrecargado de plasma."),
                        new ClassSkill("fulgor_voltaico", "Fulgor Voltaico", SkillCategory.MOVIMIENTO, ElementType.IGNEO, 14.0, 15.0, 4.0, 6, Material.FIREWORK_ROCKET, "Viaja como una centella dejando brasas."),
                        new ClassSkill(" sobrecarga_ignea".trim(), "Sobrecarga Ígnea", SkillCategory.ESPECIAL, ElementType.IGNEO, 25.0, 28.0, 5.8, 11, Material.TNT, "Detona instantáneamente la reacción Fulmen + Ígneo."),
                        new ClassSkill("ira_dios_trueno", "Ira del Dios del Trueno", SkillCategory.DEFINITIVA, ElementType.FULMEN, 52.0, 65.0, 9.0, 24, Material.END_CRYSTAL, "Definitiva: Bombardeo devastador de relámpagos reales.")
                ),
                List.of("Ascensión de Arcanista enfocada en poder destructivo puro.")
        ));

        // ==================== 4. STARTER: CUSTODIO SYLVA & EVOLUCIONES ====================
        registerClass(new RpgClassDefinition(
                "custodio_sylva", "Custodio Sylva", "Invocador / Soporte de Naturaleza y Estrellas", null,
                ElementType.SYLVA, ElementType.ASTRAL, Material.FLOWERING_AZALEA,
                Map.of(StatType.MAX_HEALTH, 30.0, StatType.MAGIC_DAMAGE, 15.0, StatType.DEFENSE, 10.0),
                List.of("druida_primordial", "oraculo_estelar"),
                List.of(
                        new ClassSkill("latigo_espinas", "Látigo de Espinas", SkillCategory.RAPIDA, ElementType.SYLVA, 9.0, 15.0, 3.2, 2, Material.VINE, "Azote floral que impregna con Sylva."),
                        new ClassSkill("brote_explosivo", "Brote Explosivo", SkillCategory.CARGADA, ElementType.SYLVA, 17.0, 24.0, 4.2, 6, Material.SPORE_BLOSSOM, "Semilla abisal que estalla liberando esporas."),
                        new ClassSkill("paso_floral", "Paso del Bosque", SkillCategory.MOVIMIENTO, ElementType.SYLVA, 9.0, 12.0, 3.5, 7, Material.OAK_LEAVES, "Impulso natural que regenera levemente la salud."),
                        new ClassSkill("santuario_vital", "Santuario Vital", SkillCategory.ESPECIAL, ElementType.ASTRAL, 15.0, 20.0, 5.5, 12, Material.GOLDEN_APPLE, "Daña a los enemigos cercanos y restaura vida a los aliados."),
                        new ClassSkill("despertar_yggdrasil", "Despertar del Árbol Mundo", SkillCategory.DEFINITIVA, ElementType.SYLVA, 32.0, 55.0, 8.0, 24, Material.CHERRY_SAPLING, "Definitiva: Enraíza a todos los enemigos y revitaliza al grupo.")
                ),
                List.of("Clase versátil que combina daño de naturaleza, control y curación.", "Evoluciona al Nv. 10 en Druida Primordial o Oráculo Estelar.")
        ));

        registerClass(new RpgClassDefinition(
                "druida_primordial", "Druida Primordial", "Señor de las Bestias y la Flora Abisal", "custodio_sylva",
                ElementType.SYLVA, ElementType.TERRA, Material.MOSS_BLOCK,
                Map.of(StatType.MAX_HEALTH, 60.0, StatType.MAGIC_DAMAGE, 28.0, StatType.DEFENSE, 18.0),
                List.of(),
                List.of(
                        new ClassSkill("zarpazo_salvaje", "Zarpazo Salvaje", SkillCategory.RAPIDA, ElementType.SYLVA, 14.0, 20.0, 3.4, 2, Material.BONE, "Ataque feroz imbuido en veneno natural."),
                        new ClassSkill("aplastamiento_raiz", "Aplastamiento de Raíces", SkillCategory.CARGADA, ElementType.TERRA, 25.0, 38.0, 4.8, 6, Material.ROOTED_DIRT, "Raíces petrificadoras que rompen la postura."),
                        new ClassSkill("embestida_feral", "Embestida Feral", SkillCategory.MOVIMIENTO, ElementType.SYLVA, 13.0, 18.0, 4.0, 6, Material.RABBIT_FOOT, "Salto depredador."),
                        new ClassSkill("nube_mortal", "Nube de Esporas Abisales", SkillCategory.ESPECIAL, ElementType.SYLVA, 22.0, 28.0, 6.0, 11, Material.FERMENTED_SPIDER_EYE, "Corroe las defensas de todos los enemigos."),
                        new ClassSkill("furia_gaia", "Furia del Bosque Primordial", SkillCategory.DEFINITIVA, ElementType.SYLVA, 44.0, 75.0, 8.5, 24, Material.AZALEA_LEAVES, "Definitiva: Terremoto viviente de raíces y esporas.")
                ),
                List.of("Ascensión de Custodio Sylva centrada en resistencia y control.")
        ));

        registerClass(new RpgClassDefinition(
                "oraculo_estelar", "Oráculo Estelar", "Sabio de Constelaciones y Sinergia Grupal", "custodio_sylva",
                ElementType.ASTRAL, ElementType.SYLVA, Material.RECOVERY_COMPASS,
                Map.of(StatType.MAX_HEALTH, 45.0, StatType.MAGIC_DAMAGE, 34.0, StatType.CRIT_CHANCE, 15.0),
                List.of(),
                List.of(
                        new ClassSkill("pulso_pulsar", "Pulso de Púlsar", SkillCategory.RAPIDA, ElementType.ASTRAL, 14.0, 18.0, 3.6, 2, Material.GLOW_BERRIES, "Proyectil estelar guiado."),
                        new ClassSkill("alineacion_cosmica", "Alineación Cósmica", SkillCategory.CARGADA, ElementType.ASTRAL, 26.0, 32.0, 5.0, 6, Material.CLOCK, "Convergencia de estrellas que purga y daña."),
                        new ClassSkill("orbita_celeste", "Órbita Celeste", SkillCategory.MOVIMIENTO, ElementType.ASTRAL, 12.0, 15.0, 4.0, 6, Material.ELYTRA, "Desplazamiento gravitatorio sin fricción."),
                        new ClassSkill("bendicion_constelacion", "Bendición de la Constelación", SkillCategory.ESPECIAL, ElementType.SYLVA, 20.0, 25.0, 6.5, 11, Material.ENCHANTED_GOLDEN_APPLE, "Restaura salud a todo el escuadrón y marca a los enemigos."),
                        new ClassSkill("decreto_firmamento", "Decreto del Firmamento", SkillCategory.DEFINITIVA, ElementType.ASTRAL, 46.0, 70.0, 9.0, 24, Material.END_ROD, "Definitiva: Lluvia de estrellas que sana aliados y diezma enemigos.")
                ),
                List.of("Ascensión de Custodio Sylva especializada en soporte ofensivo y sinergias.")
        ));
    }

    private void registerTalismans() {
        registerTalisman(new TalismanDefinition(
                "alba_carmesi", "Talismán del Alba Carmesí", Material.RED_DYE,
                Map.of(StatType.MAX_HEALTH, 35.0), null, 0.0,
                "Aumenta la Vida Máxima en +35 (estilo Favor del Árbol Áureo)."
        ));
        registerTalisman(new TalismanDefinition(
                "sello_rompeposturas", "Sello del Rompeposturas", Material.ANVIL,
                Map.of(StatType.STUN_DAMAGE, 14.0, StatType.MAX_STUN, 25.0), null, 0.0,
                "Aumenta el Daño de Stun en +14 y tu Postura Máxima en +25."
        ));
        registerTalisman(new TalismanDefinition(
                "eco_elemental", "Amuleto del Eco Elemental", Material.AMETHYST_CLUSTER,
                Map.of(StatType.MAGIC_DAMAGE, 16.0), ElementType.FULMEN, 20.0,
                "Aumenta el Daño Mágico en +16 y potencia la resonancia elemental."
        ));
        registerTalisman(new TalismanDefinition(
                "dragon_gemelo", "Emblema del Dragón Gemelo", Material.BLAZE_POWDER,
                Map.of(StatType.CRIT_CHANCE, 12.0, StatType.CRIT_DAMAGE, 30.0), ElementType.IGNEO, 15.0,
                "Aumenta +12% Prob. Crítica y +30% Daño Crítico."
        ));
        registerTalisman(new TalismanDefinition(
                "reliquia_bastion", "Reliquia del Bastión Eterno", Material.NETHERITE_SCRAP,
                Map.of(StatType.DEFENSE, 18.0, StatType.MAX_HEALTH, 20.0), ElementType.TERRA, 10.0,
                "Otorga +18 Defensa Física y +20 Vida Máxima."
        ));
        registerTalisman(new TalismanDefinition(
                "pluma_cefiro", "Pluma del Viento Cortante", Material.FEATHER,
                Map.of(StatType.MOVE_SPEED, 0.20, StatType.PHYSICAL_DAMAGE, 12.0), ElementType.CEFIRO, 15.0,
                "Otorga +0.20 Velocidad de Movimiento y +12 Daño Físico."
        ));
    }

    private void registerClass(RpgClassDefinition def) {
        classes.put(def.id().toLowerCase(), def);
    }

    private void registerTalisman(TalismanDefinition def) {
        talismans.put(def.id().toLowerCase(), def);
    }

    public RpgClassDefinition getClass(String id) {
        return id == null ? null : classes.get(id.toLowerCase());
    }

    public Collection<RpgClassDefinition> getAllClasses() {
        return Collections.unmodifiableCollection(classes.values());
    }

    public TalismanDefinition getTalisman(String id) {
        return id == null ? null : talismans.get(id.toLowerCase());
    }

    public Collection<TalismanDefinition> getAllTalismans() {
        return Collections.unmodifiableCollection(talismans.values());
    }
}
