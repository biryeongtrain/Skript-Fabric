package ch.njol.skript.events;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import ch.njol.skript.util.Time;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;

final class EventCycle20260312CompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        EvtRealTime.register();
        SimpleEvents.register();
    }

    @Test
    void realTimeEventParsesOwnedSyntax() {
        EvtRealTime event = parseEvent("at 14:20 in real time", EvtRealTime.class);

        assertEquals("at [14:20] in real time", event.toString(null, false));
        assertEquals(EvtRealTime.RealTimeEvent.class, event.getEventClasses()[0]);
    }

    @Test
    void realTimeDelayUsesSameDayWhenTargetIsStillAhead() {
        long delay = EvtRealTime.initialDelayMillis(
                Time.parse("14:20"),
                ZoneId.of("UTC"),
                Instant.parse("2026-03-12T14:19:15Z")
        );

        assertEquals(45_000L, delay);
    }

    @Test
    void realTimeDelayRollsToNextDayWhenMinuteHasPassed() {
        long delay = EvtRealTime.initialDelayMillis(
                Time.parse("14:20"),
                ZoneId.of("UTC"),
                Instant.parse("2026-03-12T14:20:01Z")
        );

        assertEquals(86_399_000L, delay);
    }

    @Test
    void simpleEventsRegistersSupportedSubset() {
        assertInstanceOf(EvtPortal.class, parseEvent("entity portal", ch.njol.skript.lang.SkriptEvent.class));
        assertInstanceOf(EvtWeatherChange.class, parseEvent("weather change to thunder", ch.njol.skript.lang.SkriptEvent.class));
        EvtBlockFertilize fertilize = parseEvent("block fertilize", EvtBlockFertilize.class);
        assertEquals(FabricEventCompatHandles.BlockFertilize.class, fertilize.getEventClasses()[0]);
    }

    @Test
    void experienceSpawnCompatibilityEventRetainsPayloadShape() {
        FabricLocation location = new FabricLocation(null, null);
        ExperienceSpawnEvent event = new ExperienceSpawnEvent(9, location);

        assertEquals(9, event.getSpawnedXP());
        assertSame(location, event.getLocation());
    }

    @Test
    void preScriptLoadEventSnapshotsConfigList() {
        List<Config> configs = new ArrayList<>();
        Config config = new Config("test", "test.sk", null);
        configs.add(config);

        PreScriptLoadEvent event = new PreScriptLoadEvent(configs);
        configs.clear();

        assertEquals(List.of(config), event.getScripts());
        assertThrows(UnsupportedOperationException.class, () -> event.getScripts().add(config));
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
}
