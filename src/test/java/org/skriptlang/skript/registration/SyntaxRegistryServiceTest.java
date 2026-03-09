package org.skriptlang.skript.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.lang.SyntaxElement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.util.Priority;

class SyntaxRegistryServiceTest {

    @Test
    void registerOrdersSyntaxesByPriorityLikeUpstreamRegistry() {
        SyntaxRegistryService registry = new SyntaxRegistryService();
        Priority base = Priority.base();
        SyntaxInfo<SyntaxElement> middle = new SyntaxInfo<>(
                SyntaxElement.class,
                new String[]{"middle"},
                "middle",
                base
        );
        SyntaxInfo<SyntaxElement> first = new SyntaxInfo<>(
                SyntaxElement.class,
                new String[]{"first"},
                "first",
                Priority.before(base)
        );
        SyntaxInfo<SyntaxElement> last = new SyntaxInfo<>(
                SyntaxElement.class,
                new String[]{"last"},
                "last",
                Priority.after(base)
        );

        registry.register("test", last);
        registry.register("test", first);
        registry.register("test", middle);

        List<SyntaxInfo<?>> registered = new ArrayList<>();
        registry.syntaxes("test").forEach(registered::add);

        assertEquals(List.of(first, middle, last), registered);
    }

    @Test
    void registerKeepsTransitiveBeforePrioritiesAheadOfBaseEntries() {
        SyntaxRegistryService registry = new SyntaxRegistryService();
        Priority base = Priority.base();
        SyntaxInfo<SyntaxElement> middle = new SyntaxInfo<>(
                SyntaxElement.class,
                new String[]{"middle"},
                "middle",
                base
        );
        SyntaxInfo<SyntaxElement> beforeMiddle = new SyntaxInfo<>(
                SyntaxElement.class,
                new String[]{"before-middle"},
                "before-middle",
                Priority.after(Priority.before(base))
        );

        registry.register("test", middle);
        registry.register("test", beforeMiddle);

        List<SyntaxInfo<?>> registered = new ArrayList<>();
        registry.syntaxes("test").forEach(registered::add);

        assertEquals(List.of(beforeMiddle, middle), registered);
    }
}
