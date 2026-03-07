package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.skriptlang.skript.bukkit.breeding.elements.EffMakeAdultOrBaby;
import org.skriptlang.skript.bukkit.displays.text.EffTextDisplayDropShadow;
import org.skriptlang.skript.bukkit.displays.text.EffTextDisplaySeeThroughBlocks;
import org.skriptlang.skript.bukkit.interactions.elements.effects.EffMakeResponsive;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompDamageable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompDispensable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompInteract;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompShearable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompSwapEquipment;
import org.skriptlang.skript.bukkit.loottables.elements.effects.EffGenerateLoot;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffApplyPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPoison;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionAmbient;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionIcon;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionParticles;
import org.skriptlang.skript.bukkit.tags.elements.EffRegisterTag;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;

final class EffectSyntaxParsingTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void failingEffectScriptsStillLoadExpectedEffectItems() throws Exception {
        Map<String, Class<?>> expectations = Map.ofEntries(
                Map.entry("skript/gametest/effect/make_adult_marks_block.sk", EffMakeAdultOrBaby.class),
                Map.entry("skript/gametest/effect/make_baby_marks_block.sk", EffMakeAdultOrBaby.class),
                Map.entry("skript/gametest/effect/text_display_add_shadow_names_entity.sk", EffTextDisplayDropShadow.class),
                Map.entry("skript/gametest/effect/text_display_make_see_through_names_entity.sk", EffTextDisplaySeeThroughBlocks.class),
                Map.entry("skript/gametest/effect/make_responsive_names_entity.sk", EffMakeResponsive.class),
                Map.entry("skript/gametest/effect/equippable_damage_effect_renames_item.sk", EffEquipCompDamageable.class),
                Map.entry("skript/gametest/effect/equippable_dispensable_effect_renames_item.sk", EffEquipCompDispensable.class),
                Map.entry("skript/gametest/effect/equippable_interact_effect_renames_item.sk", EffEquipCompInteract.class),
                Map.entry("skript/gametest/effect/equippable_shearable_effect_renames_item.sk", EffEquipCompShearable.class),
                Map.entry("skript/gametest/effect/equippable_swappable_effect_renames_item.sk", EffEquipCompSwapEquipment.class),
                Map.entry("skript/gametest/effect/generate_loot_marks_block.sk", EffGenerateLoot.class),
                Map.entry("skript/gametest/effect/apply_potion_names_entity.sk", EffApplyPotionEffect.class),
                Map.entry("skript/gametest/effect/poison_effect_names_entity.sk", EffPoison.class),
                Map.entry("skript/gametest/effect/potion_ambient_effect_names_entity.sk", EffPotionAmbient.class),
                Map.entry("skript/gametest/effect/potion_icon_effect_names_entity.sk", EffPotionIcon.class),
                Map.entry("skript/gametest/effect/potion_particles_effect_names_entity.sk", EffPotionParticles.class),
                Map.entry("skript/gametest/effect/register_custom_tag_renames_item.sk", EffRegisterTag.class)
        );

        List<Executable> assertions = new ArrayList<>();
        for (Map.Entry<String, Class<?>> expectation : expectations.entrySet()) {
            assertions.add(() -> {
                SkriptRuntime runtime = SkriptRuntime.instance();
                runtime.clearScripts();
                runtime.loadFromPath(Path.of("src/gametest/resources").resolve(expectation.getKey()));

                List<Object> items = triggerItems(onlyLoadedTrigger(runtime));
                assertFalse(items.isEmpty(), expectation.getKey() + " should load at least one trigger item");
                assertEquals(expectation.getValue(), items.getFirst().getClass(), expectation.getKey() + " should load the expected first effect");
            });
        }
        assertAll(assertions);
    }

    private Trigger onlyLoadedTrigger(SkriptRuntime runtime) throws ReflectiveOperationException {
        Field scriptsField = SkriptRuntime.class.getDeclaredField("scripts");
        scriptsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Script> scripts = (List<Script>) scriptsField.get(runtime);
        Script script = scripts.getFirst();
        Structure structure = script.getStructures().getFirst();
        return ((ch.njol.skript.lang.SkriptEvent) structure).getTrigger();
    }

    private List<Object> triggerItems(Trigger trigger) throws ReflectiveOperationException {
        Field firstField = TriggerSection.class.getDeclaredField("first");
        Field nextField = TriggerItem.class.getDeclaredField("next");
        firstField.setAccessible(true);
        nextField.setAccessible(true);

        List<Object> items = new ArrayList<>();
        Object current = firstField.get(trigger);
        while (current != null) {
            items.add(current);
            current = nextField.get(current);
        }
        return items;
    }
}
