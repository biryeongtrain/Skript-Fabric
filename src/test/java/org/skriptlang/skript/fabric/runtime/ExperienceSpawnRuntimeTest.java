package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

final class ExperienceSpawnRuntimeTest {

    private static final List<Integer> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        Skript.registerEffect(RecordExperienceAmountEffect.class, "record experience amount");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        EVENTS.clear();
    }

    @Test
    void bridgeDispatchesExperienceSpawnEventToRuntime() throws Exception {
        Path script = Files.createTempFile("evt-experience-spawn", ".sk");
        Files.writeString(
                script,
                """
                on experience spawn:
                    record experience amount
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);

        SkriptFabricEventBridge.dispatchExperienceSpawn(allocate(ServerLevel.class), allocateExperienceOrb(9));
        assertEquals(List.of(9), EVENTS);
    }

    private static TestExperienceOrb allocateExperienceOrb(int value) throws Exception {
        TestExperienceOrb orb = allocate(TestExperienceOrb.class);
        orb.value = value;
        return orb;
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

    public static final class RecordExperienceAmountEffect extends Effect {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (event.handle() instanceof FabricEventCompatHandles.ExperienceSpawn handle) {
                EVENTS.add(handle.amount());
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record experience amount";
        }
    }

    private static final class TestExperienceOrb extends ExperienceOrb {

        private int value;

        private TestExperienceOrb() {
            super((Level) null, 0.0D, 0.0D, 0.0D, 0);
        }

        @Override
        public int getValue() {
            return value;
        }
    }
}
