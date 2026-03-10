package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CondPermissionCompatibilityTest {

    @Test
    void matchesPermissionSupportsDirectAndWildcardSkriptPermissions() {
        Set<String> granted = Set.of("skript.tree.*", "skript.*", "skript.direct");

        assertTrue(CondPermission.matchesPermission("skript.direct", granted::contains));
        assertTrue(CondPermission.matchesPermission("skript.tree.leaf", granted::contains));
        assertTrue(CondPermission.matchesPermission("skript.anything.here", granted::contains));
        assertFalse(CondPermission.matchesPermission("example.permission", granted::contains));
        assertFalse(CondPermission.matchesPermission("example.child.node", Set.of("example.*")::contains));
    }
}
