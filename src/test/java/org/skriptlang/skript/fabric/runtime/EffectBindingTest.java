package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.effects.EffDoIf;
import ch.njol.skript.effects.EffEquip;
import ch.njol.skript.effects.EffHealth;
import java.lang.reflect.Field;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.skriptlang.skript.bukkit.breeding.elements.EffMakeAdultOrBaby;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.displays.text.EffTextDisplayDropShadow;
import org.skriptlang.skript.bukkit.displays.text.EffTextDisplaySeeThroughBlocks;
import org.skriptlang.skript.bukkit.interactions.elements.effects.EffMakeResponsive;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompDamageable;
import org.skriptlang.skript.bukkit.loottables.elements.effects.EffGenerateLoot;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffApplyPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPoison;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionAmbient;
import org.skriptlang.skript.bukkit.tags.elements.EffRegisterTag;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class EffectBindingTest {

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
    void displayShadowFixtureBindsEntityTargetAndPositiveMode() throws Exception {
        EffTextDisplayDropShadow effect = loadFirstEffect("skript/gametest/effect/text_display_add_shadow_names_entity.sk", EffTextDisplayDropShadow.class);
        assertTrue(readBoolean(effect, "addShadow"));
        assertEquals("event-entity", expression(effect, "displays").toString(null, false));
    }

    @Test
    void displaySeeThroughFixtureBindsEntityTargetAndPositiveMode() throws Exception {
        EffTextDisplaySeeThroughBlocks effect = loadFirstEffect("skript/gametest/effect/text_display_make_see_through_names_entity.sk", EffTextDisplaySeeThroughBlocks.class);
        assertTrue(readBoolean(effect, "canSee"));
        assertEquals("event-entity", expression(effect, "displays").toString(null, false));
    }

    @Test
    void responsiveFixtureBindsEntityTargetAndPositiveMode() throws Exception {
        EffMakeResponsive effect = loadFirstEffect("skript/gametest/effect/make_responsive_names_entity.sk", EffMakeResponsive.class);
        assertTrue(!readBoolean(effect, "negated"));
        assertEquals("event-entity", expression(effect, "entities").toString(null, false));
    }

    @Test
    void makeChildFixtureBindsEntityTargetAndBabyMode() throws Exception {
        EffMakeAdultOrBaby effect = loadFirstEffect("skript/gametest/effect/force_event_entity_to_become_child_marks_block.sk", EffMakeAdultOrBaby.class);
        assertFalse(readBoolean(effect, "adult"));
        assertEquals("event-entity", expression(effect, "entities").toString(null, false));
    }

    @Test
    void equippableFixtureBindsEquippableComponentExpression() throws Exception {
        EffEquipCompDamageable effect = loadFirstEffect("skript/gametest/effect/equippable_damage_effect_renames_item.sk", EffEquipCompDamageable.class);
        assertTrue(readBoolean(effect, "loseDurability"));
        assertEquals("event-item", expression(effect, "values").toString(null, false));
    }

    @Test
    void lootFixtureBindsLootTableAndBlockTarget() throws Exception {
        EffGenerateLoot effect = loadFirstEffect("skript/gametest/effect/generate_loot_marks_block.sk", EffGenerateLoot.class);
        assertEquals("event-block", expression(effect, "targets").toString(null, false));
        assertNotNull(expression(effect, "lootTables").getSingle(null));
    }

    @Test
    void applyPotionFixtureBindsPotionExpressionEntityTargetAndDuration() throws Exception {
        EffApplyPotionEffect effect = loadFirstEffect("skript/gametest/effect/apply_potion_names_entity.sk", EffApplyPotionEffect.class);
        assertEquals("event-entity", expression(effect, "entities").toString(null, false));
        assertNotNull(expression(effect, "duration"));
        assertNotNull(expression(effect, "potions").getSingle(null));
    }

    @Test
    void poisonFixtureBindsEntityTargetAndDuration() throws Exception {
        EffPoison effect = loadFirstEffect("skript/gametest/effect/poison_effect_names_entity.sk", EffPoison.class);
        assertEquals("event-entity", expression(effect, "entities").toString(null, false));
        assertNotNull(expression(effect, "duration"));
        assertTrue(!readBoolean(effect, "cure"));
    }

    @Test
    void potionAmbientFixtureBindsPotionExpression() throws Exception {
        EffPotionAmbient effect = loadFirstEffect("skript/gametest/effect/potion_ambient_effect_names_entity.sk", EffPotionAmbient.class);
        String rendered = expression(effect, "potions").toString(null, false);
        assertTrue(rendered.contains("poison"));
        assertTrue(rendered.contains("event-entity"));
        assertTrue(!readBoolean(effect, "negated"));
    }

    @Test
    void registerTagFixtureBindsStringNameAndItemContents() throws Exception {
        EffRegisterTag effect = loadFirstEffect("skript/gametest/effect/register_custom_tag_renames_item.sk", EffRegisterTag.class);
        assertTrue(expression(effect, "name").toString(null, false).contains("effect_test_items"));
        assertEquals("event-item", expression(effect, "contents").toString(null, false));
        assertEquals("ITEM", readObject(effect, "target").toString());
    }

    @Test
    void delayFixtureBindsTimespanExpression() throws Exception {
        Delay effect = loadFirstEffect("skript/gametest/effect/wait_one_tick_sets_block.sk", Delay.class);
        String rendered = expression(effect, "duration").toString(null, false);
        assertTrue(rendered.contains("1 tick"));
    }

    @Test
    void doIfFixtureBindsInnerEffectAndCondition() throws Exception {
        EffDoIf effect = loadFirstEffect("skript/gametest/effect/do_if_names_entity.sk", EffDoIf.class);
        assertNotNull(readObject(effect, "effect"));
        assertNotNull(readObject(effect, "condition"));
    }

    @Test
    void equipFixtureBindsEventEntityAndItemType() throws Exception {
        EffEquip effect = loadFirstEffect("skript/gametest/effect/equip_entity_marks_block.sk", EffEquip.class);
        assertEquals("event-entity", expression(effect, "entities").toString(null, false));
        assertTrue(expression(effect, "itemTypes").toString(null, false).contains("diamond"));
    }

    @Test
    void healthFixtureBindsDamageTargetAndAmount() throws Exception {
        EffHealth effect = loadFirstEffect("skript/gametest/effect/damage_entity_marks_block.sk", EffHealth.class);
        assertEquals("event-entity", expression(effect, "damageables").toString(null, false));
        assertEquals(2, ((Number) expression(effect, "amount").getSingle(null)).intValue());
    }

    private <T> T loadFirstEffect(String resourcePath, Class<T> effectClass) throws Exception {
        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.clearScripts();
        runtime.loadFromPath(Path.of("src/gametest/resources").resolve(resourcePath));
        Trigger trigger = onlyLoadedTrigger(runtime);
        Object first = firstTriggerItem(trigger);
        assertInstanceOf(effectClass, first);
        return effectClass.cast(first);
    }

    private Trigger onlyLoadedTrigger(SkriptRuntime runtime) throws ReflectiveOperationException {
        Field scriptsField = SkriptRuntime.class.getDeclaredField("scripts");
        scriptsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<Script> scripts = (java.util.List<Script>) scriptsField.get(runtime);
        Script script = scripts.getFirst();
        Structure structure = script.getStructures().getFirst();
        return ((ch.njol.skript.lang.SkriptEvent) structure).getTrigger();
    }

    private Object firstTriggerItem(Trigger trigger) throws ReflectiveOperationException {
        Field firstField = TriggerSection.class.getDeclaredField("first");
        firstField.setAccessible(true);
        TriggerItem first = (TriggerItem) firstField.get(trigger);
        assertNotNull(first);
        return first;
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readObject(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Object readObject(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(owner);
    }

    private Field findField(Class<?> owner, String fieldName) throws NoSuchFieldException {
        Class<?> current = owner;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
