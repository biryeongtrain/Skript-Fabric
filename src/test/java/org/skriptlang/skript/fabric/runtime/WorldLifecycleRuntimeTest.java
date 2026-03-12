package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

final class WorldLifecycleRuntimeTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        Skript.registerEffect(RecordWorldLifecycleEffect.class, "record world lifecycle");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        EVENTS.clear();
    }

    @Test
    void serverWorldLifecycleCallbacksDispatchLoadAndUnloadTriggers() throws Exception {
        Path script = Files.createTempFile("evt-world-lifecycle", ".sk");
        Files.writeString(
                script,
                """
                on world loading:
                    record world lifecycle

                on world unloading:
                    record world lifecycle
                """
        );

        SkriptRuntime.instance().loadFromPath(script);

        ServerLevel level = allocate(ServerLevel.class);

        ServerWorldEvents.LOAD.invoker().onWorldLoad(null, level);
        ServerWorldEvents.UNLOAD.invoker().onWorldUnload(null, level);

        assertEquals(List.of("LOAD", "UNLOAD"), EVENTS);
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        return (T) unsafe().allocateInstance(type);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    public static final class RecordWorldLifecycleEffect extends Effect {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (event.handle() instanceof FabricEventCompatHandles.World handle) {
                EVENTS.add(handle.action().name());
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record world lifecycle";
        }
    }
}
