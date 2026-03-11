package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class ScheduledEventRuntimeTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        Skript.registerEffect(RecordScheduledEffect.class, "record scheduled %string%");
    }

    @AfterEach
    void clearRuntime() {
        EVENTS.clear();
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void periodicalScriptExecutesOnMatchingTicks() throws IOException {
        Path script = Files.createTempFile("evt-periodical", ".sk");
        Files.writeString(
                script,
                """
                every 2 ticks:
                    record scheduled "periodical"
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);

        runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricScheduledTickHandle(null, 1, 0),
                null,
                null,
                null
        ));
        assertEquals(List.of(), EVENTS);

        runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricScheduledTickHandle(null, 2, 0),
                null,
                null,
                null
        ));
        assertEquals(List.of("periodical"), EVENTS);

        runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricScheduledTickHandle(null, 3, 0),
                null,
                null,
                null
        ));
        assertEquals(List.of("periodical"), EVENTS);
    }

    @Test
    void atTimeScriptExecutesWhenDayTimeCrossesTarget() throws IOException {
        Path script = Files.createTempFile("evt-at-time", ".sk");
        Files.writeString(
                script,
                """
                at 6:00:
                    record scheduled "at-time"
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);

        runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricScheduledTickHandle(null, 5, 5),
                null,
                null,
                null
        ));
        assertEquals(List.of(), EVENTS);

        runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricScheduledTickHandle(null, 24000, 0),
                null,
                null,
                null
        ));
        assertEquals(List.of("at-time"), EVENTS);
    }

    public static final class RecordScheduledEffect extends Effect {

        private Expression<String> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            value = (Expression<String>) expressions[0];
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            @Nullable String entry = value.getSingle(event);
            if (entry != null) {
                EVENTS.add(entry);
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record scheduled " + value.toString(event, debug);
        }
    }
}
