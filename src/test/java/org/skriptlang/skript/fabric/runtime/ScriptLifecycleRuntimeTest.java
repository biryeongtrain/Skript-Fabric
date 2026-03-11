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

final class ScriptLifecycleRuntimeTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        Skript.registerEffect(RecordLifecycleEffect.class, "record lifecycle %string%");
        Skript.registerEffect(RecordLifecycleHandleEffect.class, "record lifecycle-handle");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        EVENTS.clear();
    }

    @Test
    void scriptLoadAndUnloadEventsExecuteLifecycleTriggers() throws IOException {
        Path script = Files.createTempFile("evt-script", ".sk");
        Files.writeString(
                script,
                """
                on load:
                    record lifecycle "load"
                    record lifecycle-handle

                on unload:
                    record lifecycle "unload"
                    record lifecycle-handle
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);
        assertEquals(List.of("load", "ScriptEvent"), EVENTS);

        runtime.clearScripts();
        assertEquals(List.of("load", "ScriptEvent", "unload", "ScriptEvent"), EVENTS);
    }

    @Test
    void skriptStartAndStopEventsExecuteOnRuntimeTransitions() throws IOException {
        Path script = Files.createTempFile("evt-skript", ".sk");
        Files.writeString(
                script,
                """
                on skript start:
                    record lifecycle "start"
                    record lifecycle-handle

                on skript stop:
                    record lifecycle "stop"
                    record lifecycle-handle
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);
        assertEquals(List.of("start", "SkriptStartEvent"), EVENTS);

        runtime.clearScripts();
        assertEquals(List.of("start", "SkriptStartEvent", "stop", "SkriptStopEvent"), EVENTS);
    }

    public static final class RecordLifecycleEffect extends Effect {

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
            return "record lifecycle " + value.toString(event, debug);
        }
    }

    public static final class RecordLifecycleHandleEffect extends Effect {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            Object handle = event.handle();
            EVENTS.add(handle == null ? "null" : handle.getClass().getSimpleName());
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record lifecycle-handle";
        }
    }
}
