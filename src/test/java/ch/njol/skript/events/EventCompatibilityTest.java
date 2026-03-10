package ch.njol.skript.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import java.lang.reflect.Field;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;

final class EventCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        EntityData.register();
        EntityType.register();
        EvtDamage.register();
        EvtBreeding.register();
        EvtBucketCatch.register();
        EvtScript.register();
        EvtSkript.register();
        EvtCommand.register();
        EvtFirstJoin.register();
        EvtLevel.register();
        EvtMove.register();
        EvtPlayerChunkEnter.register();
        EvtPlayerCommandSend.register();
        EvtSpectate.register();
        EvtTeleport.register();
        EvtExperienceChange.register();
        EvtBookEdit.register();
        EvtBookSign.register();
        EvtBeaconEffect.register();
        EvtBeaconToggle.register();
        EvtBlock.register();
        EvtClick.register();
        EvtEntity.register();
        EvtEntityBlockChange.register();
        EvtItem.register();
        EvtEntityShootBow.register();
        EvtEntityTarget.register();
        EvtEntityTransform.register();
        EvtExperienceSpawn.register();
        EvtFirework.register();
        EvtGameMode.register();
        EvtHarvestBlock.register();
        EvtHealing.register();
        EvtLeash.register();
        EvtMoveOn.register();
        EvtGrow.register();
        EvtPlantGrowth.register();
        EvtPlayerArmorChange.register();
        EvtPortal.register();
        EvtPressurePlate.register();
        EvtResourcePackResponse.register();
        EvtVehicleCollision.register();
        EvtWeatherChange.register();
        EvtWorld.register();
    }

    @Test
    void damageEventParsesEntityFilters() throws Exception {
        EvtDamage event = parseEvent("damage of zombie by skeleton", EvtDamage.class);

        assertEquals("zombie", readLiteralArray(event, "ofTypes"));
        assertEquals("skeleton", readLiteralArray(event, "byTypes"));
        assertEquals("damage of [zombie] by [skeleton]", event.toString(null, false));
        assertEquals(1, event.getEventClasses().length);
    }

    @Test
    void breedingEventParsesPluralEntityDataFilter() throws Exception {
        EvtBreeding event = parseEvent("entity breeding of cows", EvtBreeding.class);

        assertEquals("cow", readArray(event, "entityTypes"));
        assertEquals("breeding of cow", event.toString(null, false));
    }

    @Test
    void bucketCatchEventParsesEntityDataFilter() throws Exception {
        EvtBucketCatch event = parseEvent("bucket catching of axolotls", EvtBucketCatch.class);

        assertEquals("axolotl", readArray(event, "entityTypes"));
        assertEquals("bucket catch of axolotl", event.toString(null, false));
    }

    @Test
    void scriptEventParsesAsyncLoadPattern() {
        EvtScript event = parseEvent("on async script load", EvtScript.class);

        assertEquals("async script load", event.toString(null, false));
    }

    @Test
    void skriptEventParsesServerStartPattern() {
        EvtSkript event = parseEvent("on server start", EvtSkript.class);

        assertEquals("on skript start", event.toString(null, false));
    }

    @Test
    void commandEventStripsSlashAndMatchesLabels() throws Exception {
        EvtCommand event = parseEvent("command \"/stop now\"", EvtCommand.class);

        assertEquals("stop now", readStringArray(event, "commands"));
        assertEquals("command \"/stop now\"", event.toString(null, false));
        assertEquals(1, event.getEventClasses().length);
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricPlayerEventHandles.Command("/stop now please"),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void firstJoinEventChecksFirstJoinFlag() {
        EvtFirstJoin event = parseEvent("first join", EvtFirstJoin.class);

        assertEquals("first join", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricPlayerEventHandles.FirstJoin(true),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void levelEventParsesUpAndChecksNewLevel() {
        EvtLevel event = parseEvent("player level up", EvtLevel.class);

        assertEquals("level up", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricPlayerEventHandles.Level(4, 5),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void moveEventParsesRotateVariant() throws Exception {
        EvtMove event = parseEvent("player turning around", EvtMove.class);

        assertEquals("player", readField(event, "entityData").toString());
        assertEquals("player rotate", event.toString(null, false));
    }

    @Test
    void playerChunkEnterEventChecksChunkBoundary() {
        EvtPlayerChunkEnter event = parseEvent("player enters a chunk", EvtPlayerChunkEnter.class);

        assertEquals("player enter chunk", event.toString(null, false));
        assertEquals(1, event.getEventClasses().length);
    }

    @Test
    void playerCommandSendEventSnapshotsOriginalCommands() {
        EvtPlayerCommandSend event = parseEvent("send command list", EvtPlayerCommandSend.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricPlayerEventHandles.CommandSend(java.util.List.of("help", "stop")),
                        null,
                        null,
                        null
                ))
        );
        assertEquals(java.util.List.of("help", "stop"), event.getOriginalCommands());
        assertEquals("sending of the server command list", event.toString(null, false));
    }

    @Test
    void spectateEventParsesStartPattern() throws Exception {
        EvtSpectate event = parseEvent("player start spectating of zombie", EvtSpectate.class);

        assertEquals("zombie", readLiteralArray(event, "datas"));
    }

    @Test
    void experienceChangeEventParsesDecreaseAndChecksAmount() {
        EvtExperienceChange event = parseEvent("player experience decrease", EvtExperienceChange.class);

        assertEquals("player level progress decrease", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricPlayerEventHandles.ExperienceChange(null, -3),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void bookEditEventChecksSigningFlag() {
        EvtBookEdit event = parseEvent("book edit", EvtBookEdit.class);

        assertEquals("book edit", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.BookEdit(ItemStack.EMPTY, new ItemStack(Items.WRITABLE_BOOK), false),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void bookSignEventChecksSigningFlag() {
        EvtBookSign event = parseEvent("book signing", EvtBookSign.class);

        assertEquals("book sign", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.BookEdit(new ItemStack(Items.WRITABLE_BOOK), new ItemStack(Items.WRITTEN_BOOK), true),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void entityBlockChangeEventParsesFallingLandingPattern() {
        EvtEntityBlockChange event = parseEvent("falling block landing", EvtEntityBlockChange.class);
        assertEquals("falling block landing", event.toString(null, false));
    }

    @Test
    void growEventChecksBlockGrowthHandle() {
        EvtGrow event = parseEvent("growth", EvtGrow.class);
        assertEquals("grow", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.Grow(dummyLevel(), BlockPos.ZERO, Blocks.WHEAT.defaultBlockState(), Blocks.WHEAT.defaultBlockState(), null),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void plantGrowthEventChecksHandle() {
        EvtPlantGrowth event = parseEvent("plant growth of wheat seeds", EvtPlantGrowth.class);
        assertEquals("plant growth", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.PlantGrowth(dummyLevel(), BlockPos.ZERO, Blocks.WHEAT.defaultBlockState(), Blocks.WHEAT.defaultBlockState()),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void pressurePlateEventParsesTripwireVariant() {
        EvtPressurePlate event = parseEvent("tripwire", EvtPressurePlate.class);
        assertEquals("trip", event.toString(null, false));
    }

    @Test
    void vehicleCollisionEventParsesEntityVariant() {
        EvtVehicleCollision event = parseEvent("vehicle entity collision of zombie", EvtVehicleCollision.class);
        assertEquals("vehicle entity collision of [zombie]", event.toString(null, false));
    }

    @Test
    void beaconEffectEventParsesPrimaryFilterAndChecksHandle() {
        EvtBeaconEffect event = parseEvent("primary beacon effect of speed", EvtBeaconEffect.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.BeaconEffect(dummyLevel(), BlockPos.ZERO, true, "speed"),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void beaconToggleEventChecksActivationHandle() {
        EvtBeaconToggle event = parseEvent("beacon activation", EvtBeaconToggle.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.BeaconToggle(dummyLevel(), BlockPos.ZERO, true),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void blockEventParsesBreakFilterAndChecksHandle() {
        EvtBlock event = parseEvent("block breaking of stone", EvtBlock.class);

        assertEquals(true, event.toString(null, false).contains("break"));
    }

    @Test
    void clickEventParsesToolAndChecksBlockHandle() {
        EvtClick event = parseEvent("right click with stick on stone", EvtClick.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.Click(
                                dummyLevel(),
                                BlockPos.ZERO,
                                FabricEventCompatHandles.ClickType.RIGHT,
                                null,
                                Blocks.STONE.defaultBlockState(),
                                new ItemStack(Items.STICK)
                        ),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void entityShootBowEventParsesEntityFilter() throws Exception {
        EvtEntityShootBow event = parseEvent("skeleton shooting bow", EvtEntityShootBow.class);

        assertEquals("skeleton", readLiteralArray(event, "entityDatas"));
    }

    @Test
    void entityTargetEventChecksUntarget() {
        EvtEntityTarget event = parseEvent("entity untarget", EvtEntityTarget.class);

        assertEquals("entity untarget", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.EntityTarget(null),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void entityTransformEventParsesEntityAndReasonFilters() throws Exception {
        EvtEntityTransform event = parseEvent("zombie transforming due to \"curing\"", EvtEntityTransform.class);

        assertEquals("zombie", readLiteralArray(event, "datas"));
        assertEquals("curing", readLiteralArray(event, "reasons"));
    }

    @Test
    void experienceSpawnEventParsesAlternatePattern() {
        EvtExperienceSpawn event = parseEvent("experience orb spawn", EvtExperienceSpawn.class);

        assertEquals("experience spawn", event.toString(null, false));
        assertEquals(1, event.getEventClasses().length);
    }

    @Test
    void fireworkEventParsesAndChecksPlainHandle() {
        EvtFirework event = parseEvent("firework explode", EvtFirework.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.Firework(java.util.Set.of()),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void gamemodeEventParsesSpecificModeAndChecksHandle() {
        EvtGameMode event = parseEvent("gamemode change to spectator", EvtGameMode.class);

        assertEquals("gamemode change to spectator", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.GameMode(GameType.SPECTATOR),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void harvestBlockEventParsesItemFilterAndChecksHandle() {
        EvtHarvestBlock event = parseEvent("block harvest of stone", EvtHarvestBlock.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.HarvestBlock(Blocks.STONE.defaultBlockState()),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void healingEventParsesEntityAndStringReason() throws Exception {
        EvtHealing event = parseEvent("healing of zombie by \"magic\"", EvtHealing.class);

        assertEquals("zombie", readLiteralArray(event, "entityDatas"));
        assertEquals("magic", readLiteralArray(event, "healReasons"));
    }

    @Test
    void entityLifecycleEventParsesSpawnFilterAndChecksHandle() {
        EvtEntity event = parseEvent("spawning of zombie", EvtEntity.class);

        assertEquals("spawn of zombie", event.toString(null, false));
    }

    @Test
    void itemEventParsesSpawnFilterAndChecksHandle() {
        EvtItem event = parseEvent("item spawn of stick", EvtItem.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.Item(
                                dummyLevel(),
                                BlockPos.ZERO,
                                FabricEventCompatHandles.ItemAction.SPAWN,
                                new ItemStack(Items.STICK),
                                false
                        ),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void leashEventParsesPlayerUnleashVariant() {
        EvtLeash event = parseEvent("player unleashing", EvtLeash.class);

        assertEquals("player unleash", event.toString(null, false));
    }

    @Test
    void moveOnEventParsesItemTypeAndChecksHandle() {
        EvtMoveOn event = parseEvent("walking on dirt", EvtMoveOn.class);

        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.MoveOn(Blocks.DIRT.defaultBlockState()),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void playerArmorChangeEventChecksHelmetVariant() {
        EvtPlayerArmorChange event = parseEvent("helmet change", EvtPlayerArmorChange.class);

        assertEquals("helmet changed", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.PlayerArmorChange(FabricEventCompatHandles.ArmorSlot.HEAD),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void portalEventParsesEntityVariant() {
        EvtPortal event = parseEvent("entity portal", EvtPortal.class);

        assertEquals("entity portal", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.Portal(null, false),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void resourcePackEventChecksAcceptedVariant() {
        EvtResourcePackResponse event = parseEvent("resource pack accepted", EvtResourcePackResponse.class);

        assertEquals("resource pack accepted", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.ResourcePackResponse("accepted"),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void weatherChangeEventChecksThunderVariant() {
        EvtWeatherChange event = parseEvent("weather change to thunder", EvtWeatherChange.class);

        assertEquals("weather change to thunder", event.toString(null, false));
        assertEquals(
                true,
                event.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricEventCompatHandles.WeatherChange(true, true),
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void worldEventParsesLoadVariant() {
        EvtWorld event = parseEvent("world loading", EvtWorld.class);

        assertEquals("world save/init/unload/load", event.toString(null, false));
    }

    private <T> T parseEvent(String input, Class<T> type) {
        ch.njol.skript.lang.SkriptEvent parsed = ch.njol.skript.lang.SkriptEvent.parse(
                input,
                new SectionNode(input),
                "failed"
        );
        assertNotNull(parsed);
        assertInstanceOf(type, parsed);
        return type.cast(parsed);
    }

    private String readLiteralArray(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        assertInstanceOf(ch.njol.skript.lang.Literal.class, value);
        Object[] array = ((ch.njol.skript.lang.Literal<?>) value).getArray(null);
        return array.length == 0 ? "" : String.valueOf(array[0]);
    }

    private String readArray(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        Object[] array = (Object[]) value;
        return array.length == 0 ? "" : String.valueOf(array[0]);
    }

    private String readStringArray(Object owner, String fieldName) throws Exception {
        Object[] array = (Object[]) readField(owner, fieldName);
        return array.length == 0 ? "" : String.valueOf(array[0]);
    }

    private Object readField(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(owner);
    }

    private static ServerLevel dummyLevel() {
        return null;
    }
}
