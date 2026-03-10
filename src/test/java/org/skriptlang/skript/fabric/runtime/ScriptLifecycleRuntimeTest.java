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
        Skript.registerEffect(RecordLifecycleEffect.class, "record lifecycle %string%");
    }

    @AfterEach
    void clearRuntime() {
        EVENTS.clear();
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void scriptLoadAndUnloadEventsExecuteLifecycleTriggers() throws IOException {
        Path script = Files.createTempFile("evt-script", ".sk");
        Files.writeString(
                script,
                """
                on load:
                    record lifecycle "load"

                on unload:
                    record lifecycle "unload"
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);
        assertEquals(List.of("load"), EVENTS);

        runtime.clearScripts();
        assertEquals(List.of("load", "unload"), EVENTS);
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
}
