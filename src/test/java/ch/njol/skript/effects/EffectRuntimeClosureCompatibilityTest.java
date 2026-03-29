package ch.njol.skript.effects;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
public final class EffectRuntimeClosureCompatibilityTest {

    private static boolean expressionsRegistered;
    private static List<SyntaxInfo<?>> originalEffects = List.of();

    @BeforeAll
    static void bootstrapSyntax() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        originalEffects = new ArrayList<>();
        for (SyntaxInfo<?> effectInfo : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT)) {
            originalEffects.add(effectInfo);
        }
        ensureSyntax();
    }

    @AfterAll
    static void restoreRuntimeSyntax() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        for (SyntaxInfo<?> effectInfo : originalEffects) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EFFECT, effectInfo);
        }
    }

    private static void ensureSyntax() {
        if (!expressionsRegistered) {
            registerClassInfo(Entity.class, "entity");
            registerClassInfo(LivingEntity.class, "livingentity");
            registerClassInfo(ServerPlayer.class, "player");
            registerClassInfo(Number.class, "number");
            registerClassInfo(FabricBlock.class, "block");
            registerClassInfo(FabricLocation.class, "location");
            registerClassInfo(FabricInventory.class, "inventory");
            registerClassInfo(FabricItemType.class, "itemtype");
            registerClassInfo(ServerLevel.class, "world");
            registerClassInfo(Vec3.class, "vector");
            registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-f-test-block");
            Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-f-test-location");
            Skript.registerExpression(TestInventoryExpression.class, FabricInventory.class, "lane-f-test-inventory");
            Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-f-test-itemtype");
            Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-f-test-world");
            Skript.registerExpression(TestVectorExpression.class, Vec3.class, "lane-f-test-vector");
            Skript.registerExpression(TestTimespanExpression.class, ch.njol.skript.util.Timespan.class, "lane-f-test-timespan");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        Skript.registerEffect(EffEndermanTeleport.class,
                "make %livingentities% (randomly teleport|teleport randomly)",
                "force %livingentities% to (randomly teleport|teleport randomly)",
                "make %livingentities% teleport [randomly] towards %entity%",
                "force %livingentities% to teleport [randomly] towards %entity%");
        Skript.registerEffect(EffForceAttack.class,
                "make %livingentities% attack %entities%",
                "force %livingentities% to attack %entities%",
                "make %livingentities% damage %entities% by %number% [heart[s]]",
                "force %livingentities% to damage %entities% by %number% [heart[s]]");
        Skript.registerEffect(EffPathfind.class,
                "make %livingentities% (pathfind|move) to[wards] %livingentity/location% [at speed %-number%]",
                "make %livingentities% stop (pathfinding|moving)");
        Skript.registerEffect(EffPersistent.class,
                "make %entities/blocks% [:not] persist[ent]",
                "force %entities/blocks% to [:not] persist",
                "prevent %entities/blocks% from persisting");
        Skript.registerEffect(EffToggleFlight.class,
                "(allow|enable) (fly|flight) (for|to) %players%",
                "(disallow|disable) (fly|flight) (for|to) %players%");
        Skript.registerEffect(EffTransform.class, "(transform|map) %~objects% (using|with) <.+>");
        Skript.registerEffect(EffVehicle.class,
                "(make|let|force) %entities% [to] (ride|mount) [(in|on)] %entity/entitydata%",
                "(make|let|force) %entities% [to] (dismount|(dismount|leave) (from|of|) (any|the[ir]|his|her|) vehicle[s])",
                "(eject|dismount) (any|the|) passenger[s] (of|from) %entities%",
                "eject passenger[s] (of|from) %entities%");
        Skript.registerEffect(EffZombify.class,
                "zombify %livingentities%",
                "unzombify %livingentities% [(in|after) %-timespan%]");
        Skript.registerEffect(EffCommandBlockConditional.class,
                "make command block[s] %blocks% [not:(un|not )]conditional");
        Skript.registerEffect(EffGlowingText.class,
                "make %blocks/itemtypes% have glowing text",
                "make %blocks/itemtypes% have (normal|non[-| ]glowing) text");
        Skript.registerEffect(
                EffBan.class,
                "ban [kick:and kick] %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
                "unban %strings/offlineplayers%",
                "ban [kick:and kick] %players% by IP [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
                "unban %players% by IP",
                "IP(-| )ban [kick:and kick] %players% [(by reason of|because [of]|on account of|due to) %-string%] [for %-timespan%]",
                "(IP(-| )unban|un[-]IP[-]ban) %players%"
        );
        Skript.registerEffect(
                EffCancelItemUse.class,
                "(cancel|interrupt) [the] us[ag]e of %livingentities%'[s] [active|current] item"
        );
        Skript.registerEffect(
                EffCommand.class,
                "[execute] [the] [bungee:bungee[cord]] command[s] %strings% [by %-players%]",
                "[execute] [the] %players% [bungee:bungee[cord]] command[s] %strings%",
                "(let|make) %players% execute [[the] [bungee:bungee[cord]] command[s]] %strings%"
        );
        Skript.registerEffect(
                EffLidState.class,
                "(open|:close) [the] lid[s] (of|for) %blocks%",
                "(open|:close) %blocks%'[s] lid[s]"
        );
        Skript.registerEffect(
                EffLook.class,
                "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %entity%'s (feet:feet|eyes) [(at|with) [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]",
                "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) [the] (feet:feet|eyes) of %entity% [(at|with) [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]",
                "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %vector/location/entity% [(at|with) [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]"
        );
        Skript.registerEffect(EffOpenBook.class, "(open|show) book %itemtype% (to|for) %players%");
        Skript.registerEffect(
                EffOpenInventory.class,
                "close %players%'[s] inventory [view]",
                "close [the] inventory [view] (to|of|for) %players%",
                "open %inventory% (to|for) %players%",
                "open [a] (crafting table|workbench) (to|for) %players%",
                "open [a] chest (to|for) %players%",
                "open [a[n]] anvil (to|for) %players%",
                "open [a] hopper (to|for) %players%",
                "open [a] dropper (to|for) %players%",
                "open [a] dispenser (to|for) %players%"
        );
        Skript.registerEffect(EffPvP.class, "enable PvP [in %worlds%]", "disable PVP [in %worlds%]");
        Skript.registerEffect(
                EffSendBlockChange.class,
                "make %players% see %locations% as [the|its] (original|normal|actual) [block]",
                "make %players% see %locations% as %itemtype/objects%"
        );
        Skript.registerEffect(
                EffTooltip.class,
                "(show|reveal|:hide) %itemtypes%'[s] [entire|:additional] tool[ ]tip",
                "(show|reveal|:hide) [the] [entire|:additional] tool[ ]tip of %itemtypes%"
        );
        Skript.registerEffect(EffWardenDisturbance.class, "make %livingentities% sense [a] disturbance at %location%");
        Skript.registerEffect(EffWorldSave.class, "save [[the] world[s]] %worlds%");
        Skript.registerEffect(EffStopServer.class, "(stop|shut[ ]down) [the] server", "restart [the] server");
        Skript.registerEffect(
                EffBlockUpdate.class,
                "update %blocks% (as|to be) %objects% [physics:without [neighbo[u]r[ing]|adjacent] [physics] update[s]]"
        );
        Skript.registerEffect(EffBreakNaturally.class, "break %blocks% [naturally] [using %-itemtype%]");
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void endermanTeleportEffectBindsOptionalTarget() throws Exception {
        EffEndermanTeleport random = parseEffect("make lane-f-test-livingentity teleport randomly", EffEndermanTeleport.class);
        EffEndermanTeleport targeted = parseEffect("force lane-f-test-livingentity to teleport towards lane-f-test-entity", EffEndermanTeleport.class);

        assertEquals("lane-f-test-livingentity", expression(random, "entities").toString(null, false));
        assertEquals("lane-f-test-entity", expression(targeted, "target").toString(null, false));
    }

    @Test
    void forceAttackEffectBindsVictimsAndDamageAmount() throws Exception {
        EffForceAttack attack = parseEffect("make lane-f-test-livingentity attack lane-f-test-entity", EffForceAttack.class);
        EffForceAttack damage = parseEffect("force lane-f-test-livingentity to damage lane-f-test-entity by lane-f-test-number hearts", EffForceAttack.class);

        assertEquals("lane-f-test-entity", expression(attack, "victims").toString(null, false));
        assertEquals("lane-f-test-number", expression(damage, "amount").toString(null, false));
    }

    @Test
    void pathfindEffectBindsTargetAndStopPattern() throws Exception {
        EffPathfind move = parseEffect("make lane-f-test-livingentity pathfind towards lane-f-test-location at speed lane-f-test-number", EffPathfind.class);
        EffPathfind stop = parseEffect("make lane-f-test-livingentity stop pathfinding", EffPathfind.class);

        assertEquals("lane-f-test-location", expression(move, "target").toString(null, false));
        assertEquals("lane-f-test-number", expression(move, "speed").toString(null, false));
        assertEquals(null, readField(stop, "target"));
    }

    @Test
    void persistentEffectTracksNegatedMode() throws Exception {
        EffPersistent persist = parseEffect("make lane-f-test-block persistent", EffPersistent.class);
        EffPersistent prevent = parseEffect("prevent lane-f-test-block from persisting", EffPersistent.class);

        assertTrue(readBoolean(persist, "persist"));
        assertFalse(readBoolean(prevent, "persist"));
    }

    @Test
    void toggleFlightEffectTracksAllowAndDisallowPatterns() throws Exception {
        EffToggleFlight allow = parseEffect("allow flight to lane-f-test-player", EffToggleFlight.class);
        EffToggleFlight disallow = parseEffect("disable flight for lane-f-test-player", EffToggleFlight.class);

        assertTrue(readBoolean(allow, "allow"));
        assertFalse(readBoolean(disallow, "allow"));
    }

    @Test
    void transformEffectParsesListVariableAndMappingExpression() throws Exception {
        EffTransform transform = parseEffect("transform {lane-f-test::*} with 0", EffTransform.class);

        assertEquals("{lane-f-test::*}", readField(transform, "unmappedObjects").toString());
        assertNotNull(readField(transform, "mappingExpr"));
    }

    @Test
    void vehicleEffectTracksRideAndDismountPatterns() throws Exception {
        EffVehicle ride = parseEffect("make lane-f-test-entity ride lane-f-test-entity", EffVehicle.class);
        EffVehicle dismount = parseEffect("make lane-f-test-entity dismount", EffVehicle.class);
        EffVehicle eject = parseEffect("eject passengers of lane-f-test-entity", EffVehicle.class);

        assertNotNull(expression(ride, "passengers"));
        assertEquals(null, readField(dismount, "vehicles"));
        assertEquals(null, readField(eject, "passengers"));
    }

    @Test
    void zombifyEffectTracksModeAndOptionalTimespan() throws Exception {
        EffZombify zombify = parseEffect("zombify lane-f-test-livingentity", EffZombify.class);
        EffZombify unzombify = parseEffect("unzombify lane-f-test-livingentity after lane-f-test-timespan", EffZombify.class);

        assertTrue(readBoolean(zombify, "zombify"));
        assertFalse(readBoolean(unzombify, "zombify"));
        assertEquals("lane-f-test-timespan", expression(unzombify, "timespan").toString(null, false));
    }

    @Test
    void commandBlockConditionalEffectTracksNegatedPattern() throws Exception {
        EffCommandBlockConditional conditional = parseEffect("make command block lane-f-test-block conditional", EffCommandBlockConditional.class);
        EffCommandBlockConditional unconditional = parseEffect("make command block lane-f-test-block unconditional", EffCommandBlockConditional.class);

        assertTrue(readBoolean(conditional, "conditional"));
        assertFalse(readBoolean(unconditional, "conditional"));
    }

    @Test
    void glowingTextEffectTracksGlowingAndNormalPatterns() throws Exception {
        EffGlowingText glowing = parseEffect("make lane-f-test-block have glowing text", EffGlowingText.class);
        EffGlowingText normal = parseEffect("make lane-f-test-block have normal text", EffGlowingText.class);

        assertTrue(readBoolean(glowing, "glowing"));
        assertFalse(readBoolean(normal, "glowing"));
    }

    @Test
    void banEffectTracksIpKickAndExpiry() throws Exception {
        EffBan effect = parseEffect("IP-ban and kick lane-f-test-player because \"x\" for lane-f-test-timespan", EffBan.class);

        assertTrue(readBoolean(effect, "ban"));
        assertTrue(readBoolean(effect, "ipBan"));
        assertTrue(readBoolean(effect, "kick"));
        assertEquals("lane-f-test-timespan", expression(effect, "expires").toString(null, false));
    }

    @Test
    void cancelItemUseBindsLivingEntities() throws Exception {
        EffCancelItemUse effect = parseEffect("interrupt the usage of lane-f-test-livingentity's active item", EffCancelItemUse.class);

        assertEquals("lane-f-test-livingentity", expression(effect, "entities").toString(null, false));
    }

    @Test
    void commandEffectBindsOptionalSender() throws Exception {
        EffCommand console = parseEffect("execute command \"/say test\"", EffCommand.class);
        EffCommand player = parseEffect("make lane-f-test-player execute command \"/spawn\"", EffCommand.class);

        assertEquals(null, readField(console, "senders"));
        assertEquals("lane-f-test-player", expression(player, "senders").toString(null, false));
    }

    @Test
    void lidStateTracksOpenAndCloseModes() throws Exception {
        EffLidState open = parseEffect("open the lid of lane-f-test-block", EffLidState.class);
        EffLidState close = parseEffect("close lane-f-test-block's lid", EffLidState.class);

        assertTrue(readBoolean(open, "setOpen"));
        assertFalse(readBoolean(close, "setOpen"));
    }

    @Test
    void lookEffectBindsTargetAndOptionalNumbers() throws Exception {
        EffLook effect = parseEffect("make lane-f-test-livingentity look at lane-f-test-vector at speed lane-f-test-number and max pitch lane-f-test-number", EffLook.class);

        assertEquals("lane-f-test-vector", expression(effect, "target").toString(null, false));
        assertEquals("lane-f-test-number", expression(effect, "speed").toString(null, false));
        assertEquals("lane-f-test-number", expression(effect, "maxPitch").toString(null, false));
    }

    @Test
    void openBookBindsBookAndPlayers() throws Exception {
        EffOpenBook effect = parseEffect("open book lane-f-test-itemtype to lane-f-test-player", EffOpenBook.class);

        assertEquals("lane-f-test-itemtype", expression(effect, "book").toString(null, false));
        assertEquals("lane-f-test-player", expression(effect, "players").toString(null, false));
    }

    @Test
    void openInventoryTracksInventoryAndMenuPatterns() throws Exception {
        EffOpenInventory inventory = parseEffect("open lane-f-test-inventory to lane-f-test-player", EffOpenInventory.class);
        EffOpenInventory close = parseEffect("close lane-f-test-player's inventory", EffOpenInventory.class);
        EffOpenInventory chest = parseEffect("open chest to lane-f-test-player", EffOpenInventory.class);

        assertTrue(readBoolean(inventory, "open"));
        assertEquals("lane-f-test-inventory", expression(inventory, "inventory").toString(null, false));
        assertFalse(readBoolean(close, "open"));
        assertEquals("chest", readField(chest, "menuType"));
    }

    @Test
    void pvpEffectTracksEnableAndDisableModes() throws Exception {
        EffPvP enable = parseEffect("enable PvP in lane-f-test-world", EffPvP.class);
        EffPvP disable = parseEffect("disable PvP in lane-f-test-world", EffPvP.class);

        assertTrue(readBoolean(enable, "enable"));
        assertFalse(readBoolean(disable, "enable"));
    }

    @Test
    void sendBlockChangeTracksOriginalAndExplicitTypeModes() throws Exception {
        EffSendBlockChange explicit = parseEffect("make lane-f-test-player see lane-f-test-location as lane-f-test-itemtype", EffSendBlockChange.class);
        EffSendBlockChange original = parseEffect("make lane-f-test-player see lane-f-test-location as the original block", EffSendBlockChange.class);

        assertFalse(readBoolean(explicit, "asOriginal"));
        assertEquals("lane-f-test-itemtype", expression(explicit, "type").toString(null, false));
        assertTrue(readBoolean(original, "asOriginal"));
    }

    @Test
    void tooltipTracksHideAndEntireModes() throws Exception {
        EffTooltip hideEntire = parseEffect("hide lane-f-test-itemtype's tooltip", EffTooltip.class);
        EffTooltip showAdditional = parseEffect("show the additional tooltip of lane-f-test-itemtype", EffTooltip.class);

        assertTrue(readBoolean(hideEntire, "hide"));
        assertTrue(readBoolean(hideEntire, "entire"));
        assertFalse(readBoolean(showAdditional, "hide"));
        assertFalse(readBoolean(showAdditional, "entire"));
    }

    @Test
    void wardenDisturbanceBindsLocation() throws Exception {
        EffWardenDisturbance effect = parseEffect("make lane-f-test-livingentity sense a disturbance at lane-f-test-location", EffWardenDisturbance.class);

        assertEquals("lane-f-test-location", expression(effect, "location").toString(null, false));
    }

    @Test
    void worldSaveAndStopServerBindDirectModes() throws Exception {
        EffWorldSave save = parseEffect("save lane-f-test-world", EffWorldSave.class);
        EffStopServer restart = parseEffect("restart server", EffStopServer.class);

        assertEquals("lane-f-test-world", expression(save, "worlds").toString(null, false));
        assertTrue(readBoolean(restart, "restart"));
    }

    @Test
    void blockUpdateAndBreakNaturallyBindOptionalTooling() throws Exception {
        EffBlockUpdate update = parseEffect("update lane-f-test-block as lane-f-test-itemtype", EffBlockUpdate.class);
        EffBreakNaturally tool = parseEffect("break lane-f-test-block naturally using lane-f-test-itemtype", EffBreakNaturally.class);

        assertEquals("lane-f-test-itemtype", expression(update, "blockData").toString(null, false));
        assertEquals("lane-f-test-itemtype", expression(tool, "tool").toString(null, false));
    }

    private <T extends Effect> T parseEffect(String input, Class<T> effectClass) {
        Statement statement = Statement.parse(input, "failed");
        assertNotNull(statement);
        assertInstanceOf(effectClass, statement);
        return effectClass.cast(statement);
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Object readField(Object owner, String fieldName) throws Exception {
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

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {

        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-player";
        }
    }

    public static final class TestEntityExpression extends SimpleExpression<Entity> {

        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Entity> getReturnType() {
            return Entity.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-entity";
        }
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {

        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-livingentity";
        }
    }

    public static final class TestNumberExpression extends SimpleExpression<Number> {

        @Override
        protected Number @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Number> getReturnType() {
            return Number.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-number";
        }
    }

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {

        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-block";
        }
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {

        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends FabricLocation> getReturnType() {
            return FabricLocation.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-location";
        }
    }

    public static final class TestTimespanExpression extends SimpleExpression<ch.njol.skript.util.Timespan> {

        @Override
        protected ch.njol.skript.util.Timespan @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends ch.njol.skript.util.Timespan> getReturnType() {
            return ch.njol.skript.util.Timespan.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-timespan";
        }
    }

    public static final class TestInventoryExpression extends SimpleExpression<FabricInventory> {

        @Override
        protected FabricInventory @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends FabricInventory> getReturnType() {
            return FabricInventory.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-inventory";
        }
    }

    public static final class TestItemTypeExpression extends SimpleExpression<FabricItemType> {

        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-itemtype";
        }
    }

    public static final class TestWorldExpression extends SimpleExpression<ServerLevel> {

        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends ServerLevel> getReturnType() {
            return ServerLevel.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-world";
        }
    }

    public static final class TestVectorExpression extends SimpleExpression<Vec3> {

        @Override
        protected Vec3 @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public Class<? extends Vec3> getReturnType() {
            return Vec3.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-f-test-vector";
        }
    }
}
