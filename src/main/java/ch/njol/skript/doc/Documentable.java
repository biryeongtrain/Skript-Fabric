package ch.njol.skript.doc;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public interface Documentable {

    @NotNull String name();

    @Unmodifiable
    @NotNull List<String> description();

    @Unmodifiable
    @NotNull List<String> since();

    @Unmodifiable
    @NotNull List<String> examples();

    @Unmodifiable
    @NotNull List<String> keywords();

    @Unmodifiable
    @NotNull List<String> requires();
}
