package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.variables.Variables;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("isolated-registry")
final class EffectStorageRuntimeTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        Variables.clearAll();
    }

    @Test
    void sortEffectReordersLoadedScriptListVariables() throws IOException {
        Path script = Files.createTempFile("eff-sort-runtime", ".sk");
        Files.writeString(
                script,
                """
                on item spawn:
                    delete {values::*}
                    set {values::1} to "c"
                    set {values::2} to "a"
                    set {values::3} to "b"
                    sort {values::*}
                """
        );

        SkriptRuntime.instance().loadFromPath(script);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new ch.njol.skript.events.FabricEventCompatHandles.Item(
                        null,
                        BlockPos.ZERO,
                        ch.njol.skript.events.FabricEventCompatHandles.ItemAction.SPAWN,
                        new ItemStack(Items.APPLE),
                        false
                ),
                null,
                null,
                null
        ));

        assertEquals("a", Variables.getVariable("values::1", org.skriptlang.skript.lang.event.SkriptEvent.EMPTY, false));
        assertEquals("b", Variables.getVariable("values::2", org.skriptlang.skript.lang.event.SkriptEvent.EMPTY, false));
        assertEquals("c", Variables.getVariable("values::3", org.skriptlang.skript.lang.event.SkriptEvent.EMPTY, false));
    }
}
