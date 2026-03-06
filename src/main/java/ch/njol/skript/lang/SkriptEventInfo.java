package ch.njol.skript.lang;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.StructureInfo;

@Deprecated(since = "2.14", forRemoval = true)
public class SkriptEventInfo<E extends SkriptEvent> extends StructureInfo<E> {

    public final Class<?>[] events;
    public final String name;
    private SkriptEvent.ListeningBehavior listeningBehavior;
    private final String id;
    private @Nullable String documentationId;

    public SkriptEventInfo(String name, String[] patterns, Class<E> eventClass, String originClassPath, Class<?>[] events) {
        super(patterns, eventClass, originClassPath);
        this.events = events;
        if (name.startsWith("*")) {
            this.name = name.substring(1);
        } else {
            this.name = "On " + name;
        }
        this.id = this.name.toLowerCase(Locale.ENGLISH).replaceAll("\\s+", "_");
        this.listeningBehavior = SkriptEvent.ListeningBehavior.ANY;
    }

    public SkriptEventInfo<E> listeningBehavior(SkriptEvent.ListeningBehavior behavior) {
        this.listeningBehavior = behavior;
        return this;
    }

    public SkriptEvent.ListeningBehavior getListeningBehavior() {
        return listeningBehavior;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public SkriptEventInfo<E> documentationID(String id) {
        this.documentationId = id;
        return this;
    }

    public @Nullable String getDocumentationID() {
        return documentationId;
    }
}
