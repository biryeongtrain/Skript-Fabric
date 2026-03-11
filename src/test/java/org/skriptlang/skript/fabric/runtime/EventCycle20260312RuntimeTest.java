package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import ch.njol.skript.Skript;
import ch.njol.skript.events.EvtRealTime;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.lang.reflect.Method;
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
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

final class EventCycle20260312RuntimeTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        EvtRealTime.register();
        Skript.registerEffect(RecordRealTimeEffect.class, "record real time");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        EVENTS.clear();
    }

    @Test
    void realTimeEventExecutesLoadedTriggerWhenInvoked() throws Exception {
        Path scriptPath = Files.createTempFile("evt-real-time", ".sk");
        Files.writeString(
                scriptPath,
                """
                at 14:20 in real time:
                    record real time
                """
        );

        Script script = SkriptRuntime.instance().loadFromPath(scriptPath);
        EvtRealTime event = findRealTimeEvent(script);

        Method execute = EvtRealTime.class.getDeclaredMethod("execute");
        execute.setAccessible(true);
        execute.invoke(event);

        assertEquals(List.of("real-time"), EVENTS);
    }

    private static EvtRealTime findRealTimeEvent(Script script) {
        for (Structure structure : script.getStructures()) {
            if (structure instanceof EvtRealTime event) {
                return event;
            }
        }
        fail("Expected a loaded EvtRealTime structure");
        return null;
    }

    public static final class RecordRealTimeEffect extends Effect {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (event.handle() instanceof EvtRealTime.RealTimeEvent) {
                EVENTS.add("real-time");
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record real time";
        }
    }
}
