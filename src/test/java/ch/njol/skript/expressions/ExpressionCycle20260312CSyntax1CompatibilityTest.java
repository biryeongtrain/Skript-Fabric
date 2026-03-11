package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

final class ExpressionCycle20260312CSyntax1CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ensureSyntax();
    }

    @Test
    void parserBindsLaneSyntaxSubset() {
        assertInstanceOf(ExprTool.class, parseExpression("tool of lane-c12c-s1-livingentity", Slot.class));
        assertInstanceOf(ExprTool.class, parseExpression("off hand tool of lane-c12c-s1-livingentity", Slot.class));
        assertInstanceOf(ExprWithFireResistance.class, parseExpression("lane-c12c-s1-itemtype with fire resistance", FabricItemType.class));
        assertInstanceOf(ExprWithFireResistance.class, parseExpression("fire resistant lane-c12c-s1-itemtype", FabricItemType.class));
    }

    @Test
    void toolExpressionReadsAndMutatesHandSlots() throws Exception {
        Pig pig = allocateEntity(Pig.class);
        installEquipment(pig);
        setEquipmentItem(pig, EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
        setEquipmentItem(pig, EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));

        ExprTool mainHand = new ExprTool();
        assertTrue(mainHand.init(
                new Expression[]{new SimpleLiteral<>(pig, false)},
                0,
                Kleenean.FALSE,
                parseResult("tool")
        ));
        Slot mainSlot = mainHand.getSingle(SkriptEvent.EMPTY);
        assertNotNull(mainSlot);
        assertEquals(Items.STICK, mainSlot.getItem().getItem());
        mainSlot.set(new ItemStack(Items.DIAMOND_SWORD));
        assertEquals(Items.DIAMOND_SWORD, pig.getItemBySlot(EquipmentSlot.MAINHAND).getItem());

        ExprTool offHand = new ExprTool();
        assertTrue(offHand.init(
                new Expression[]{new SimpleLiteral<>(pig, false)},
                2,
                Kleenean.FALSE,
                parseResult("off hand tool")
        ));
        Slot offHandSlot = offHand.getSingle(SkriptEvent.EMPTY);
        assertNotNull(offHandSlot);
        assertEquals(Items.SHIELD, offHandSlot.getItem().getItem());
        offHandSlot.set(ItemStack.EMPTY);
        assertTrue(pig.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty());
    }

    @Test
    void withFireResistanceCreatesDerivedItemTypes() {
        FabricItemType plain = new FabricItemType(Items.IRON_INGOT);
        ExprWithFireResistance with = new ExprWithFireResistance();
        assertTrue(with.init(
                new Expression[]{new SimpleLiteral<>(plain, false)},
                0,
                Kleenean.FALSE,
                parseResult("with fire resistance")
        ));
        FabricItemType resistant = with.getSingle(SkriptEvent.EMPTY);
        assertNotNull(resistant);
        assertNotNull(resistant.toStack().get(DataComponents.DAMAGE_RESISTANT));
        assertNull(plain.toStack().get(DataComponents.DAMAGE_RESISTANT));

        ItemStack sourceStack = new ItemStack(Items.DIAMOND_SWORD);
        sourceStack.set(DataComponents.DAMAGE_RESISTANT, new net.minecraft.world.item.component.DamageResistant(net.minecraft.tags.DamageTypeTags.IS_FIRE));
        FabricItemType originalResistant = new FabricItemType(sourceStack);
        ExprWithFireResistance without = new ExprWithFireResistance();
        SkriptParser.ParseResult withoutParse = parseResult("without fire resistance");
        withoutParse.tags.add("out");
        assertTrue(without.init(
                new Expression[]{new SimpleLiteral<>(originalResistant, false)},
                0,
                Kleenean.FALSE,
                withoutParse
        ));
        FabricItemType notResistant = without.getSingle(SkriptEvent.EMPTY);
        assertNotNull(notResistant);
        assertNull(notResistant.toStack().get(DataComponents.DAMAGE_RESISTANT));
        assertNotNull(originalResistant.toStack().get(DataComponents.DAMAGE_RESISTANT));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(FabricItemType.class, "itemtype");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-c12c-s1-livingentity");
        Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-c12c-s1-itemtype");
        new ExprTool();
        new ExprWithFireResistance();
        syntaxRegistered = true;
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static SkriptParser.ParseResult parseResult(String expression) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expression;
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends LivingEntity> T allocateEntity(Class<T> type) throws Exception {
        return (T) unsafe().allocateInstance(type);
    }

    private static void installEquipment(LivingEntity entity) throws Exception {
        equipmentField().set(entity, entityEquipmentConstructor().newInstance());
    }

    private static void setEquipmentItem(LivingEntity entity, EquipmentSlot slot, ItemStack stack) throws Exception {
        Object equipment = equipmentField().get(entity);
        Method set = equipment.getClass().getDeclaredMethod("set", EquipmentSlot.class, ItemStack.class);
        set.setAccessible(true);
        set.invoke(equipment, slot, stack);
    }

    private static Field equipmentField() throws Exception {
        for (Field field : LivingEntity.class.getDeclaredFields()) {
            if (field.getType().getName().equals("net.minecraft.world.entity.EntityEquipment")) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Could not find living entity equipment field");
    }

    private static Constructor<?> entityEquipmentConstructor() throws Exception {
        Class<?> type = Class.forName("net.minecraft.world.entity.EntityEquipment");
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor;
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }
    }

    public static final class TestItemTypeExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }
    }
}
