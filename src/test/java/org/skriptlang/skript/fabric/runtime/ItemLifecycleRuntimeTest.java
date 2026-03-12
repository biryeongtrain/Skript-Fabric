package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.events.EvtItem;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

final class ItemLifecycleRuntimeTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        EvtItem.register();
        Skript.registerEffect(RecordItemLifecycleEffect.class, "record item lifecycle %string%");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        EVENTS.clear();
    }

    @Test
    void bridgeDispatchesItemDespawnAndMergeEventsToRuntime() throws Exception {
        Path script = Files.createTempFile("evt-item-lifecycle", ".sk");
        Files.writeString(
                script,
                """
                on item despawn of apple:
                    record item lifecycle "despawn"
                on item merge of apple:
                    record item lifecycle "merge"
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);

        ServerLevel level = allocate(ServerLevel.class);
        SkriptFabricEventBridge.dispatchItemDespawn(level, BlockPos.ZERO, new ItemStack(Items.APPLE));
        SkriptFabricEventBridge.dispatchItemMerge(level, BlockPos.ZERO, new ItemStack(Items.APPLE));

        assertEquals(List.of("despawn", "merge"), EVENTS);
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

    public static final class RecordItemLifecycleEffect extends Effect {

        private Expression<String> label;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            label = (Expression<String>) expressions[0];
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            String value = label.getSingle(event);
            if (value != null) {
                EVENTS.add(value);
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record item lifecycle";
        }
    }
}
