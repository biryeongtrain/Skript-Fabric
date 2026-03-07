package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompDamageable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.loottables.elements.effects.EffGenerateLoot;
import org.skriptlang.skript.bukkit.tags.TagSupport;
import org.skriptlang.skript.bukkit.tags.elements.EffRegisterTag;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.event.SkriptEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EffectSupportUnitTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void registerTagEffectNormalizesQuotedNames() throws Exception {
        ItemStack stack = new ItemStack(Items.STICK);
        EffRegisterTag effect = new EffRegisterTag();
        boolean initialized = effect.init(
                new Expression<?>[]{
                        new ConstantExpression<>(String.class, "\"effect_test_items\""),
                        new ConstantExpression<>(ItemStack.class, stack)
                },
                0,
                Kleenean.FALSE,
                new ParseResult()
        );
        assertTrue(initialized);

        execute(effect, SkriptEvent.EMPTY);

        assertTrue(TagSupport.isTagged(stack, "skript:effect_test_items"));
    }

    @Test
    void equippableDamageEffectMutatesLiveItemStack() throws Exception {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        stack.set(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.HEAD)
                        .setDamageOnHurt(false)
                        .setDispensable(false)
                        .setEquipOnInteract(false)
                        .setCanBeSheared(false)
                        .setSwappable(false)
                        .build()
        );

        EffEquipCompDamageable effect = new EffEquipCompDamageable();
        boolean initialized = effect.init(
                new Expression<?>[]{new ConstantExpression<>(ItemStack.class, stack)},
                0,
                Kleenean.FALSE,
                new ParseResult()
        );
        assertTrue(initialized);

        execute(effect, SkriptEvent.EMPTY);

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        assertTrue(equippable != null && equippable.damageOnHurt());
    }

    @Test
    void equippableDamageFixtureMutatesEventItemStack() throws Exception {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        stack.set(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.HEAD)
                        .setDamageOnHurt(false)
                        .setDispensable(false)
                        .setEquipOnInteract(false)
                        .setCanBeSheared(false)
                        .setSwappable(false)
                        .build()
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.clearScripts();
        runtime.loadFromPath(Path.of("src/gametest/resources/skript/gametest/effect/equippable_damage_effect_renames_item.sk"));
        Object effect = firstTriggerItem(runtime);
        SkriptEvent event = new SkriptEvent(new FakeItemHandle(stack), null, null, null);
        Expression<?> values = (Expression<?>) readField(effect, "values");
        Object[] resolved = values.getAll(event);

        assertEquals(1, resolved.length);
        assertInstanceOf(ItemStack.class, resolved[0]);

        execute(effect, event);

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        assertTrue(equippable != null && equippable.damageOnHurt());
        runtime.clearScripts();
    }

    @Test
    void generateLootEffectInsertsFallbackLootIntoContainer() throws Exception {
        SimpleContainer chest = new SimpleContainer(27);
        EffGenerateLoot effect = new EffGenerateLoot();
        LootContextWrapper context = new LootContextWrapper(new FabricLocation(null, Vec3.ZERO));
        boolean initialized = effect.init(
                new Expression<?>[]{
                        new ConstantExpression<>(LootTable.class, LootTable.fromId(net.minecraft.resources.ResourceLocation.parse("minecraft:chests/simple_dungeon"))),
                        new ConstantExpression<>(LootContextWrapper.class, context),
                        new ConstantExpression<>(SimpleContainer.class, chest)
                },
                1,
                Kleenean.FALSE,
                new ParseResult()
        );
        assertTrue(initialized);

        execute(effect, SkriptEvent.EMPTY);

        boolean foundLoot = false;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            if (!chest.getItem(slot).isEmpty()) {
                foundLoot = true;
                break;
            }
        }
        assertTrue(foundLoot);
    }

    private void execute(Object effect, SkriptEvent event) throws Exception {
        Method execute = effect.getClass().getSuperclass().getDeclaredMethod("execute", SkriptEvent.class);
        execute.setAccessible(true);
        execute.invoke(effect, event);
    }

    private Object firstTriggerItem(SkriptRuntime runtime) throws Exception {
        Field scriptsField = SkriptRuntime.class.getDeclaredField("scripts");
        scriptsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Script> scripts = (List<Script>) scriptsField.get(runtime);
        Script script = scripts.getFirst();
        Structure structure = script.getStructures().getFirst();
        Trigger trigger = ((ch.njol.skript.lang.SkriptEvent) structure).getTrigger();
        Field firstField = TriggerSection.class.getDeclaredField("first");
        firstField.setAccessible(true);
        TriggerItem first = (TriggerItem) firstField.get(trigger);
        return first;
    }

    private Object readField(Object instance, String name) throws Exception {
        Class<?> type = instance.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static final class ConstantExpression<T> extends SimpleExpression<T> {

        private final Class<? extends T> returnType;
        private final T[] values;

        @SafeVarargs
        private ConstantExpression(Class<? extends T> returnType, T... values) {
            this.returnType = returnType;
            this.values = values;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean isSingle() {
            return values.length == 1;
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }
    }

    private record FakeItemHandle(ItemStack itemStack) implements FabricItemEventHandle {
    }
}
