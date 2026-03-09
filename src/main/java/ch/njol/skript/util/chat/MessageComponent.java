package ch.njol.skript.util.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class MessageComponent {

    public String text = "";
    public boolean reset = false;
    public boolean bold = false;
    public boolean italic = false;
    public boolean underlined = false;
    public boolean strikethrough = false;
    public boolean obfuscated = false;
    public @Nullable String color;
    public @Nullable String insertion;
    public @Nullable ClickEvent clickEvent;
    public @Nullable String font;
    public @Nullable String translation;
    public @Nullable String keybind;
    public @Nullable HoverEvent hoverEvent;

    public static class ClickEvent {
        public ClickEvent(Action action, String value) {
            this.action = action;
            this.value = value;
        }

        public enum Action {
            open_url,
            run_command,
            suggest_command,
            change_page,
            copy_to_clipboard;

            public final String spigotName;

            Action() {
                this.spigotName = name().toUpperCase(Locale.ENGLISH);
            }
        }

        public Action action;
        public String value;
    }

    public static class HoverEvent {
        public HoverEvent(Action action, String value) {
            this.action = action;
            this.value = value;
        }

        public enum Action {
            show_text,
            show_item,
            show_entity,
            show_achievement;

            public final String spigotName;

            Action() {
                this.spigotName = name().toUpperCase(Locale.ENGLISH);
            }
        }

        public Action action;
        public String value;
    }

    public static class BooleanSerializer implements JsonSerializer<Boolean> {
        @Override
        public @Nullable JsonElement serialize(
                @Nullable Boolean src,
                @Nullable Type typeOfSrc,
                @Nullable JsonSerializationContext context
        ) {
            return Boolean.TRUE.equals(src) ? new JsonPrimitive(true) : null;
        }
    }

    public MessageComponent copy() {
        MessageComponent copy = new MessageComponent();
        copy.text = text;
        copy.reset = reset;
        copy.bold = bold;
        copy.italic = italic;
        copy.underlined = underlined;
        copy.strikethrough = strikethrough;
        copy.obfuscated = obfuscated;
        copy.color = color;
        copy.insertion = insertion;
        copy.clickEvent = clickEvent;
        copy.font = font;
        copy.translation = translation;
        copy.keybind = keybind;
        copy.hoverEvent = hoverEvent;
        return copy;
    }
}
