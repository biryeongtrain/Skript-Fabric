package ch.njol.skript.effects;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
final class EffectCompatibilityTest {

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
            registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-f-test-player");
            Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-f-test-entity");
            Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-f-test-livingentity");
            Skript.registerExpression(TestNumberExpression.class, Number.class, "lane-f-test-number");
            Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-f-test-block");
            Skript.registerExpression(TestTimespanExpression.class, ch.njol.skript.util.Timespan.class, "lane-f-test-timespan");
            expressionsRegistered = true;
        }
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EFFECT);
        Skript.registerEffect(EffFeed.class, "feed [the] %players% [by %-number% [beef[s]]]");
        Skript.registerEffect(EffKill.class, "kill %entities%");
        Skript.registerEffect(
                EffSilence.class,
                "silence %entities%",
                "unsilence %entities%",
                "make %entities% silent",
                "make %entities% not silent"
        );
        Skript.registerEffect(
                EffInvisible.class,
                "make %livingentities% not visible",
                "make %livingentities% not invisible",
                "make %livingentities% invisible",
                "make %livingentities% visible"
        );
        Skript.registerEffect(
                EffInvulnerability.class,
                "make %entities% (invulnerable|invincible)",
                "make %entities% (not (invulnerable|invincible)|vulnerable|vincible)"
        );
        Skript.registerEffect(
                EffSprinting.class,
                "make %players% (start sprinting|sprint)",
                "force %players% to (start sprinting|sprint)",
                "make %players% (stop sprinting|not sprint)",
                "force %players% to (stop sprinting|not sprint)"
        );
        Skript.registerEffect(
                EffPandaOnBack.class,
                "make %livingentities% get (:on|off) (its|their) back[s]",
                "force %livingentities% to get (:on|off) (its|their) back[s]"
        );
        Skript.registerEffect(
                EffPandaSneezing.class,
                "make %livingentities% (start:(start sneezing|sneeze)|stop sneezing)",
                "force %livingentities% to (:start|stop) sneezing"
        );
        Skript.registerEffect(
                EffPandaRolling.class,
                "make %livingentities% (start:(start rolling|roll)|stop rolling)",
                "force %livingentities% to (:start|stop) rolling"
        );
        Skript.registerEffect(
                EffScreaming.class,
                "make %livingentities% (start screaming|scream)",
                "force %livingentities% to (start screaming|scream)",
                "make %livingentities% stop screaming",
                "force %livingentities% to stop screaming"
        );
        Skript.registerEffect(
                EffStriderShivering.class,
                "make %livingentities% start shivering",
                "force %livingentities% to start shivering",
                "make %livingentities% stop shivering",
                "force %livingentities% to stop shivering"
        );
        Skript.registerEffect(
                EffCustomName.class,
                "(:show|hide) [the] (custom|display)[ ]name of %entities%",
                "(:show|hide) %entities%'[s] (custom|display)[ ]name"
        );
        Skript.registerEffect(
                EffEating.class,
                "make %livingentities% (:start|stop) eating",
                "force %livingentities% to (:start|stop) eating"
        );
        Skript.registerEffect(EffHandedness.class, "make %livingentities% (:left|right)( |-)handed");
        Skript.registerEffect(
                EffIgnite.class,
                "(ignite|set fire to) %entities% [for %-timespan%]",
                "(set|light) %entities% on fire [for %-timespan%]",
                "extinguish %entities%"
        );
        Skript.registerEffect(
                EffLeash.class,
                "(leash|lead) %livingentities% to %entity%",
                "make %entity% (leash|lead) %livingentities%",
                "un(leash|lead) [holder of] %livingentities%"
        );
        Skript.registerEffect(
                EffPlayingDead.class,
                "make %livingentities% (start playing|play) dead",
                "force %livingentities% to (start playing|play) dead",
                "make %livingentities% (stop playing|not play) dead",
                "force %livingentities% to (stop playing|not play) dead"
        );
        Skript.registerEffect(
                EffShear.class,
                "[:force] shear %livingentities%",
                "un[-]shear %livingentities%"
        );
        Skript.registerEffect(EffTame.class, "[:un](tame|domesticate) %entities%");
        Skript.registerEffect(
                EffToggleCanPickUpItems.class,
                "allow %livingentities% to pick([ ]up items| items up)",
                "(forbid|disallow) %livingentities% (from|to) pick([ing | ]up items|[ing] items up)"
        );
        Skript.registerEffect(
                EffMakeFly.class,
                "force %players% to [(start|1¦stop)] fly[ing]",
                "make %players% (start|1¦stop) flying",
                "make %players% fly"
        );
        Skript.registerEffect(
                EffContinue.class,
                "continue [this loop|[the] [current] loop]",
                "continue [the] <" + JavaClasses.INTEGER_NUMBER_PATTERN + ">(st|nd|rd|th) loop"
        );
        Skript.registerEffect(
                EffExit.class,
                "(exit|stop) [trigger]",
                "(exit|stop) [1|a|the|this] (section|1:loop|2:conditional)",
                "(exit|stop) <" + JavaClasses.INTEGER_NUMBER_PATTERN + "> (section|1:loop|2:conditional)s",
                "(exit|stop) all (section|1:loop|2:conditional)s"
        );
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void feedEffectParsesOptionalBeefAmount() throws Exception {
        EffFeed effect = parseEffect("feed lane-f-test-player by lane-f-test-number", EffFeed.class);

        assertEquals("lane-f-test-player", expression(effect, "players").toString(null, false));
        assertEquals("lane-f-test-number", expression(effect, "beefs").toString(null, false));
    }

    @Test
    void killEffectParsesEntityExpression() throws Exception {
        EffKill effect = parseEffect("kill lane-f-test-entity", EffKill.class);

        assertEquals("lane-f-test-entity", expression(effect, "entities").toString(null, false));
        assertEquals("kill lane-f-test-entity", effect.toString(null, false));
    }

    @Test
    void silenceEffectTracksMatchedPatternMode() throws Exception {
        EffSilence silence = parseEffect("silence lane-f-test-entity", EffSilence.class);
        EffSilence unsilence = parseEffect("make lane-f-test-entity not silent", EffSilence.class);

        assertTrue(readBoolean(silence, "silence"));
        assertFalse(readBoolean(unsilence, "silence"));
    }

    @Test
    void invisibilityEffectTracksVisibleAndInvisibleModes() throws Exception {
        EffInvisible invisible = parseEffect("make lane-f-test-livingentity invisible", EffInvisible.class);
        EffInvisible visible = parseEffect("make lane-f-test-livingentity visible", EffInvisible.class);

        assertTrue(readBoolean(invisible, "invisible"));
        assertFalse(readBoolean(visible, "invisible"));
    }

    @Test
    void invulnerabilityEffectTracksNegatedPattern() throws Exception {
        EffInvulnerability yes = parseEffect("make lane-f-test-entity invulnerable", EffInvulnerability.class);
        EffInvulnerability no = parseEffect("make lane-f-test-entity vulnerable", EffInvulnerability.class);

        assertTrue(readBoolean(yes, "invulnerable"));
        assertFalse(readBoolean(no, "invulnerable"));
    }

    @Test
    void sprintingEffectTracksStartAndStopPatterns() throws Exception {
        EffSprinting start = parseEffect("make lane-f-test-player sprint", EffSprinting.class);
        EffSprinting stop = parseEffect("force lane-f-test-player to stop sprinting", EffSprinting.class);

        assertTrue(readBoolean(start, "sprint"));
        assertFalse(readBoolean(stop, "sprint"));
    }

    @Test
    void pandaOnBackEffectTracksOnAndOffTags() throws Exception {
        EffPandaOnBack on = parseEffect("make lane-f-test-livingentity get on its back", EffPandaOnBack.class);
        EffPandaOnBack off = parseEffect("force lane-f-test-livingentity to get off their backs", EffPandaOnBack.class);

        assertTrue(readBoolean(on, "getOn"));
        assertFalse(readBoolean(off, "getOn"));
    }

    @Test
    void pandaSneezingEffectTracksStartTag() throws Exception {
        EffPandaSneezing start = parseEffect("make lane-f-test-livingentity sneeze", EffPandaSneezing.class);
        EffPandaSneezing stop = parseEffect("force lane-f-test-livingentity to stop sneezing", EffPandaSneezing.class);

        assertTrue(readBoolean(start, "start"));
        assertFalse(readBoolean(stop, "start"));
    }

    @Test
    void pandaRollingEffectTracksStartTag() throws Exception {
        EffPandaRolling start = parseEffect("make lane-f-test-livingentity roll", EffPandaRolling.class);
        EffPandaRolling stop = parseEffect("force lane-f-test-livingentity to stop rolling", EffPandaRolling.class);

        assertTrue(readBoolean(start, "start"));
        assertFalse(readBoolean(stop, "start"));
    }

    @Test
    void screamingEffectTracksStartAndStopPatterns() throws Exception {
        EffScreaming start = parseEffect("make lane-f-test-livingentity scream", EffScreaming.class);
        EffScreaming stop = parseEffect("force lane-f-test-livingentity to stop screaming", EffScreaming.class);

        assertTrue(readBoolean(start, "scream"));
        assertFalse(readBoolean(stop, "scream"));
    }

    @Test
    void striderShiveringEffectTracksStartAndStopPatterns() throws Exception {
        EffStriderShivering start = parseEffect("make lane-f-test-livingentity start shivering", EffStriderShivering.class);
        EffStriderShivering stop = parseEffect("force lane-f-test-livingentity to stop shivering", EffStriderShivering.class);

        assertTrue(readBoolean(start, "start"));
        assertFalse(readBoolean(stop, "start"));
    }

    @Test
    void customNameEffectTracksShowAndHidePatterns() throws Exception {
        EffCustomName show = parseEffect("show the custom name of lane-f-test-entity", EffCustomName.class);
        EffCustomName hide = parseEffect("hide lane-f-test-entity's display name", EffCustomName.class);

        assertTrue(readBoolean(show, "showCustomName"));
        assertFalse(readBoolean(hide, "showCustomName"));
    }

    @Test
    void eatingEffectTracksStartAndStopTags() throws Exception {
        EffEating start = parseEffect("make lane-f-test-livingentity start eating", EffEating.class);
        EffEating stop = parseEffect("force lane-f-test-livingentity to stop eating", EffEating.class);

        assertTrue(readBoolean(start, "start"));
        assertFalse(readBoolean(stop, "start"));
    }

    @Test
    void handednessEffectTracksLeftAndRightTags() throws Exception {
        EffHandedness left = parseEffect("make lane-f-test-livingentity left-handed", EffHandedness.class);
        EffHandedness right = parseEffect("make lane-f-test-livingentity right handed", EffHandedness.class);

        assertTrue(readBoolean(left, "leftHanded"));
        assertFalse(readBoolean(right, "leftHanded"));
    }

    @Test
    void igniteEffectTracksDurationAndExtinguishPattern() throws Exception {
        EffIgnite ignite = parseEffect("set lane-f-test-entity on fire for lane-f-test-timespan", EffIgnite.class);
        EffIgnite extinguish = parseEffect("extinguish lane-f-test-entity", EffIgnite.class);

        assertTrue(readBoolean(ignite, "ignite"));
        assertFalse(readBoolean(extinguish, "ignite"));
        assertEquals("lane-f-test-timespan", expression(ignite, "duration").toString(null, false));
    }

    @Test
    void leashEffectTracksForwardReverseAndUnleashPatterns() throws Exception {
        EffLeash forward = parseEffect("leash lane-f-test-livingentity to lane-f-test-entity", EffLeash.class);
        EffLeash reverse = parseEffect("make lane-f-test-entity leash lane-f-test-livingentity", EffLeash.class);
        EffLeash unleash = parseEffect("unleash lane-f-test-livingentity", EffLeash.class);

        assertTrue(readBoolean(forward, "leash"));
        assertTrue(readBoolean(reverse, "leash"));
        assertFalse(readBoolean(unleash, "leash"));
        assertEquals("lane-f-test-entity", expression(forward, "holder").toString(null, false));
        assertEquals("lane-f-test-livingentity", expression(reverse, "targets").toString(null, false));
    }

    @Test
    void playingDeadEffectTracksStartAndStopPatterns() throws Exception {
        EffPlayingDead start = parseEffect("make lane-f-test-livingentity play dead", EffPlayingDead.class);
        EffPlayingDead stop = parseEffect("force lane-f-test-livingentity to not play dead", EffPlayingDead.class);

        assertTrue(readBoolean(start, "playDead"));
        assertFalse(readBoolean(stop, "playDead"));
    }

    @Test
    void shearEffectTracksForceAndUnshearPatterns() throws Exception {
        EffShear force = parseEffect("force shear lane-f-test-livingentity", EffShear.class);
        EffShear unshear = parseEffect("unshear lane-f-test-livingentity", EffShear.class);

        assertTrue(readBoolean(force, "force"));
        assertTrue(readBoolean(force, "shear"));
        assertFalse(readBoolean(unshear, "shear"));
    }

    @Test
    void tameEffectTracksTameAndUntamePatterns() throws Exception {
        EffTame tame = parseEffect("tame lane-f-test-entity", EffTame.class);
        EffTame untame = parseEffect("untame lane-f-test-entity", EffTame.class);

        assertTrue(readBoolean(tame, "tame"));
        assertFalse(readBoolean(untame, "tame"));
    }

    @Test
    void togglePickUpItemsEffectTracksAllowAndForbidPatterns() throws Exception {
        EffToggleCanPickUpItems allow = parseEffect("allow lane-f-test-livingentity to pick up items", EffToggleCanPickUpItems.class);
        EffToggleCanPickUpItems forbid = parseEffect("forbid lane-f-test-livingentity from picking up items", EffToggleCanPickUpItems.class);

        assertTrue(readBoolean(allow, "allowPickUp"));
        assertFalse(readBoolean(forbid, "allowPickUp"));
    }

    @Test
    void makeFlyEffectTracksDefaultAndStopPatterns() throws Exception {
        EffMakeFly fly = parseEffect("make lane-f-test-player fly", EffMakeFly.class);
        EffMakeFly stop = parseEffect("force lane-f-test-player to stop flying", EffMakeFly.class);

        assertTrue(readBoolean(fly, "flying"));
        assertFalse(readBoolean(stop, "flying"));
    }

    @Test
    void continueEffectTargetsOuterLoopAndExitsInnerSections() {
        ParserInstance parser = ParserInstance.get();
        List<TriggerSection> previousSections = new ArrayList<>(parser.getCurrentSections());
        try {
            TrackingLoopSection outerLoop = new TrackingLoopSection("outer");
            TrackingSection innerSection = new TrackingSection("inner");
            TrackingLoopSection innerLoop = new TrackingLoopSection("inner-loop");
            parser.setCurrentSections(new ArrayList<>(List.of(outerLoop, innerSection, innerLoop)));

            EffContinue effect = parseEffect("continue the 1st loop", EffContinue.class);

            assertEquals(ExecutionIntent.stopSections(3), effect.executionIntent());
            assertEquals(outerLoop, effect.walk(SkriptEvent.EMPTY));
            assertEquals(1, innerSection.exitCount);
            assertEquals(1, innerLoop.exitCount);
            assertEquals("continue the 1st loop", effect.toString(null, false));
        } finally {
            parser.setCurrentSections(previousSections);
        }
    }

    @Test
    void exitEffectStopsNestedLoopAndReturnsOuterNext() {
        ParserInstance parser = ParserInstance.get();
        List<TriggerSection> previousSections = new ArrayList<>(parser.getCurrentSections());
        try {
            TrackingLoopSection outerLoop = new TrackingLoopSection("outer");
            TrackingSection innerSection = new TrackingSection("inner");
            TrackingLoopSection innerLoop = new TrackingLoopSection("inner-loop");
            MarkerItem afterOuter = new MarkerItem("after-outer");
            outerLoop.setNext(afterOuter);
            parser.setCurrentSections(new ArrayList<>(List.of(outerLoop, innerSection, innerLoop)));

            EffExit effect = parseEffect("stop 2 loops", EffExit.class);

            assertEquals(ExecutionIntent.stopSections(2), effect.executionIntent());
            assertEquals(afterOuter, effect.walk(SkriptEvent.EMPTY));
            assertEquals(1, outerLoop.exitCount);
            assertEquals(1, innerSection.exitCount);
            assertEquals(1, innerLoop.exitCount);
            assertEquals("stop 2 loops", effect.toString(null, false));
        } finally {
            parser.setCurrentSections(previousSections);
        }
    }

    @Test
    void exitEffectStopsTriggerWithoutSections() {
        EffExit effect = parseEffect("stop trigger", EffExit.class);

        assertInstanceOf(ExecutionIntent.StopTrigger.class, effect.executionIntent());
        assertNull(effect.walk(SkriptEvent.EMPTY));
        assertEquals("stop trigger", effect.toString(null, false));
    }

    private <T> T parseEffect(String input, Class<T> type) {
        Statement parsed = Statement.parse(input, "failed");
        assertNotNull(parsed);
        assertInstanceOf(type, parsed);
        return type.cast(parsed);
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Object readField(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(owner);
    }

    private static final class TrackingSection extends TriggerSection implements ch.njol.skript.lang.SectionExitHandler {

        private final String label;
        private int exitCount;

        private TrackingSection(String label) {
            this.label = label;
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return getNext();
        }

        @Override
        public void exit(SkriptEvent event) {
            exitCount++;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return label;
        }
    }

    private static final class TrackingLoopSection extends LoopSection {

        private final String label;
        private int exitCount;

        private TrackingLoopSection(String label) {
            this.label = label;
        }

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            return true;
        }

        @Override
        public TriggerItem getActualNext() {
            return getNext();
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return getNext();
        }

        @Override
        public void exit(SkriptEvent event) {
            super.exit(event);
            exitCount++;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return label;
        }
    }

    private static final class MarkerItem extends TriggerItem {

        private final String label;

        private MarkerItem(String label) {
            this.label = label;
        }

        @Override
        protected boolean run(SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return label;
        }
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
}
