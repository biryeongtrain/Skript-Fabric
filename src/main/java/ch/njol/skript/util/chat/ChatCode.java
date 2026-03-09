package ch.njol.skript.util.chat;

import org.jetbrains.annotations.Nullable;

public interface ChatCode {

    void updateComponent(MessageComponent component, String param);

    boolean hasParam();

    @Nullable
    String getColorCode();

    @Nullable
    String getLangName();

    default boolean isLocalized() {
        return false;
    }

    char getColorChar();
}
