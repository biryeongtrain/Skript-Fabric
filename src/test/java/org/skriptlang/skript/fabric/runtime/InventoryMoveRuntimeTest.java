package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.events.EvtItem;
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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

final class InventoryMoveRuntimeTest {

    private static final List<Item> EVENTS = new ArrayList<>();

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        EvtItem.register();
        Skript.registerEffect(RecordInventoryMoveEffect.class, "record inventory move");
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        EVENTS.clear();
    }

    @Test
    void bridgeDispatchesInventoryMoveEventToRuntime() throws Exception {
        Path script = Files.createTempFile("evt-inventory-move", ".sk");
        Files.writeString(
                script,
                """
                on inventory item move:
                    record inventory move
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);

        SkriptFabricEventBridge.dispatchInventoryMove(
                allocate(ServerLevel.class),
                BlockPos.ZERO,
                new ItemStack(Items.STICK)
        );
        assertEquals(List.of(Items.STICK), EVENTS);
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

    public static final class RecordInventoryMoveEffect extends Effect {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (event.handle() instanceof FabricEventCompatHandles.Item handle) {
                EVENTS.add(handle.itemStack().getItem());
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record inventory move";
        }
    }
}
