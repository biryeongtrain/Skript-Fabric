package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.events.EvtHarvestBlock;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

final class HarvestBlockRuntimeTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        EvtHarvestBlock.register();
        Skript.registerEffect(RecordHarvestBlockEffect.class, "record harvest block");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        EVENTS.clear();
    }

    @Test
    void bridgeDispatchesHarvestBlockEventToRuntime() throws Exception {
        Path script = Files.createTempFile("evt-harvest-block", ".sk");
        Files.writeString(
                script,
                """
                on player block harvest:
                    record harvest block
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);

        SkriptFabricEventBridge.dispatchHarvestBlock(
                allocate(ServerLevel.class),
                Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3),
                null
        );

        assertEquals(List.of("block.minecraft.sweet_berry_bush"), EVENTS);
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

    public static final class RecordHarvestBlockEffect extends Effect {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (event.handle() instanceof FabricEventCompatHandles.HarvestBlock handle) {
                EVENTS.add(handle.blockState().getBlock().getDescriptionId());
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record harvest block";
        }
    }
}
