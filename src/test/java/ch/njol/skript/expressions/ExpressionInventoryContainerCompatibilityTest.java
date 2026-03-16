package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import java.lang.reflect.Field;
import net.minecraft.SharedConstants;
import net.minecraft.world.SimpleContainer;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.event.SkriptEvent;
import sun.misc.Unsafe;

final class ExpressionInventoryContainerCompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapSyntax() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ensureSyntax();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Disabled("Moved to GameTest")
    @Test
    void parseCoverageIncludesInventoryContainerBundle() {
        assertInstanceOf(ExprOpenedInventory.class, parseExpression("open inventory of lane-m2-player", FabricInventory.class));
        assertInstanceOf(ExprArmorSlot.class, parseExpression("armor items of lane-m2-livingentity", net.minecraft.world.inventory.Slot.class));
        assertInstanceOf(ExprPickupDelay.class, parseExpression("pickup delay of lane-m2-entity", ch.njol.skript.util.Timespan.class));

        ExprCursorSlot cursorSlot = new ExprCursorSlot();
        assertTrue(cursorSlot.init(new Expression[]{new TestPlayerExpression()}, 0, ch.njol.util.Kleenean.FALSE, parseResult("cursor slot")));
        ExprHotbarSlot hotbarSlot = new ExprHotbarSlot();
        assertTrue(hotbarSlot.init(new Expression[]{new TestPlayerExpression()}, 0, ch.njol.util.Kleenean.FALSE, parseResult("current hotbar slot")));
    }

    @Disabled("Moved to GameTest")
    @Test
    void pickupDelayExpressionReadsAndMutatesItemEntities() throws Exception {
        ItemEntity itemEntity = allocateItemEntity();
        pickupDelayField().set(itemEntity, 20);

        ExprPickupDelay pickupDelay = new ExprPickupDelay();
        assertTrue(pickupDelay.init(new Expression[]{new SimpleLiteral<Entity>(itemEntity, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult("pickup delay")));
        assertEquals(20L, pickupDelay.getSingle(SkriptEvent.EMPTY).getAs(ch.njol.skript.util.Timespan.TimePeriod.TICK));

        pickupDelay.change(
                SkriptEvent.EMPTY,
                new Object[]{new ch.njol.skript.util.Timespan(ch.njol.skript.util.Timespan.TimePeriod.TICK, 5)},
                ch.njol.skript.classes.Changer.ChangeMode.ADD
        );
        assertEquals(25, pickupDelayField().get(itemEntity));
    }

    @Test
    void reflectiveInventoryBundleExpressionsWorkAgainstDummyHandles() {
        DummyAnvilHolder anvilHolder = new DummyAnvilHolder(12, 40, "rename me");
        FabricInventory anvilInventory = new FabricInventory(new SimpleContainer(3), MenuType.ANVIL, null, anvilHolder);

        ExprAnvilText anvilText = new ExprAnvilText();
        assertTrue(anvilText.init(new Expression[]{new SimpleLiteral<>(anvilInventory, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult("anvil text input")));
        assertEquals("rename me", anvilText.getSingle(SkriptEvent.EMPTY));

        ExprAnvilRepairCost repairCost = new ExprAnvilRepairCost();
        assertTrue(repairCost.init(new Expression[]{new SimpleLiteral<>(anvilInventory, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult("repair cost")));
        assertEquals(12, repairCost.getSingle(SkriptEvent.EMPTY));

        ExprAnvilRepairCost maximumRepairCost = new ExprAnvilRepairCost();
        SkriptParser.ParseResult maxResult = parseResult("maximum repair cost");
        maxResult.tags.add("max");
        assertTrue(maximumRepairCost.init(new Expression[]{new SimpleLiteral<>(anvilInventory, false)}, 0, ch.njol.util.Kleenean.FALSE, maxResult));
        maximumRepairCost.change(SkriptEvent.EMPTY, new Object[]{5}, ch.njol.skript.classes.Changer.ChangeMode.ADD);
        assertEquals(45, anvilHolder.getMaximumRepairCost());

        DummyEnchantmentOffer offer = new DummyEnchantmentOffer(8);
        ExprEnchantmentOfferCost offerCost = new ExprEnchantmentOfferCost();
        assertTrue(offerCost.init(new Expression[]{new SimpleLiteral<>(offer, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult("enchantment cost")));
        assertEquals(8L, offerCost.getSingle(SkriptEvent.EMPTY));
        offerCost.change(SkriptEvent.EMPTY, new Object[]{4}, ch.njol.skript.classes.Changer.ChangeMode.ADD);
        assertEquals(12, offer.getCost());

        ParserInstance.get().setCurrentEvent("inventory click", InventoryClickHandle.class);
        ExprHotbarButton hotbarButton = new ExprHotbarButton();
        assertTrue(hotbarButton.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("hotbar button")));
        assertEquals(6L, hotbarButton.getSingle(new SkriptEvent(new InventoryClickHandle(6, "pickup_all"), null, null, null)));

        ExprInventoryAction inventoryAction = new ExprInventoryAction();
        assertTrue(inventoryAction.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("inventory action")));
        assertEquals("pickup_all", inventoryAction.getSingle(new SkriptEvent(new InventoryClickHandle(0, "pickup_all"), null, null, null)));

        ParserInstance.get().setCurrentEvent("inventory close", InventoryCloseHandle.class);
        ExprInventoryCloseReason closeReason = new ExprInventoryCloseReason();
        assertTrue(closeReason.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("inventory close reason")));
        assertEquals("teleport", closeReason.getSingle(new SkriptEvent(new InventoryCloseHandle("teleport"), null, null, null)));

        ParserInstance.get().setCurrentEvent("armor change", ArmorChangeHandle.class);
        ExprArmorChangeItem oldArmor = new ExprArmorChangeItem();
        ExprArmorChangeItem newArmor = new ExprArmorChangeItem();
        assertTrue(oldArmor.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("old armor item")));
        assertTrue(newArmor.init(new Expression[0], 1, ch.njol.util.Kleenean.FALSE, parseResult("new armor item")));
        ArmorChangeHandle armorChange = new ArmorChangeHandle(new ItemStack(net.minecraft.world.item.Items.IRON_HELMET), new ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
        assertEquals(net.minecraft.world.item.Items.IRON_HELMET, oldArmor.getSingle(new SkriptEvent(armorChange, null, null, null)).getItem());
        assertEquals(net.minecraft.world.item.Items.DIAMOND_HELMET, newArmor.getSingle(new SkriptEvent(armorChange, null, null, null)).getItem());
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(FabricInventory.class, "inventory");
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-m2-player");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-m2-livingentity");
        Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-m2-entity");
        new ExprAnvilRepairCost();
        new ExprAnvilText();
        new ExprArmorChangeItem();
        new ExprArmorSlot();
        new ExprCursorSlot();
        new ExprEnchantmentOfferCost();
        new ExprHotbarButton();
        new ExprHotbarSlot();
        new ExprInventoryAction();
        new ExprInventoryCloseReason();
        new ExprOpenedInventory();
        new ExprPickupDelay();
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

    private static ItemEntity allocateItemEntity() throws Exception {
        return (ItemEntity) unsafe().allocateInstance(ItemEntity.class);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static Field pickupDelayField() throws Exception {
        Field field = ItemEntity.class.getDeclaredField("pickupDelay");
        field.setAccessible(true);
        return field;
    }

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {
        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }
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

    public static final class TestEntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Entity> getReturnType() {
            return Entity.class;
        }
    }

    public static final class InventoryClickHandle {

        private final int hotbarButton;
        private final String action;

        private InventoryClickHandle(int hotbarButton, String action) {
            this.hotbarButton = hotbarButton;
            this.action = action;
        }

        public int hotbarButton() {
            return hotbarButton;
        }

        public String action() {
            return action;
        }
    }

    public static final class InventoryCloseHandle {

        private final String reason;

        private InventoryCloseHandle(String reason) {
            this.reason = reason;
        }

        public String reason() {
            return reason;
        }
    }

    public static final class ArmorChangeHandle {

        private final ItemStack oldItem;
        private final ItemStack newItem;

        private ArmorChangeHandle(ItemStack oldItem, ItemStack newItem) {
            this.oldItem = oldItem;
            this.newItem = newItem;
        }

        public ItemStack oldItem() {
            return oldItem;
        }

        public ItemStack newItem() {
            return newItem;
        }
    }

    public static final class DummyEnchantmentOffer {

        private int cost;

        private DummyEnchantmentOffer(int cost) {
            this.cost = cost;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }
    }

    public static final class DummyAnvilHolder {

        private int repairCost;
        private int maximumRepairCost;
        private final String renameText;

        private DummyAnvilHolder(int repairCost, int maximumRepairCost, String renameText) {
            this.repairCost = repairCost;
            this.maximumRepairCost = maximumRepairCost;
            this.renameText = renameText;
        }

        public int getRepairCost() {
            return repairCost;
        }

        public int getMaximumRepairCost() {
            return maximumRepairCost;
        }

        public void setMaximumRepairCost(int maximumRepairCost) {
            this.maximumRepairCost = maximumRepairCost;
        }

        public String getRenameText() {
            return renameText;
        }
    }

}
