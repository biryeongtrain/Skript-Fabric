package ch.njol.skript.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricLocation;

final class FabricPlayerEventHandlesUnitTest {

    @Test
    void moveFactoryPreservesLocationsAndRotation() {
        FabricLocation from = new FabricLocation(null, null);
        FabricLocation to = new FabricLocation(null, null);

        FabricPlayerEventHandles.Move handle = (FabricPlayerEventHandles.Move) FabricPlayerEventHandles.move(
                null,
                from,
                to,
                10.0F,
                20.0F,
                30.0F,
                40.0F
        );

        assertEquals(from, handle.from());
        assertEquals(to, handle.to());
        assertEquals(10.0F, handle.fromYaw());
        assertEquals(20.0F, handle.fromPitch());
        assertEquals(30.0F, handle.toYaw());
        assertEquals(40.0F, handle.toPitch());
    }

    @Test
    void commandSendSnapshotStaysStableWhenBackingSetMutates() {
        FabricPlayerEventHandles.CommandSend handle = (FabricPlayerEventHandles.CommandSend) FabricPlayerEventHandles.commandSend(
                List.of("help", "stop")
        );

        Set<String> snapshot = handle.snapshot();
        handle.commands().add("reload");

        assertEquals(Set.of("help", "stop"), snapshot);
        assertEquals(Set.of("help", "stop", "reload"), handle.commands());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("list"));
    }

    @Test
    void commandSendDropsDuplicatesButKeepsInsertionOrder() {
        FabricPlayerEventHandles.CommandSend handle = (FabricPlayerEventHandles.CommandSend) FabricPlayerEventHandles.commandSend(
                List.of("help", "stop", "help")
        );

        assertEquals(List.of("help", "stop"), List.copyOf(handle.commands()));
        assertFalse(handle.commands().contains("reload"));
    }
}
