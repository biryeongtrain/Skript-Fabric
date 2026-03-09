package ch.njol.skript.util.chat;

import ch.njol.skript.util.chat.MessageComponent.ClickEvent;
import ch.njol.skript.util.chat.MessageComponent.HoverEvent;
import org.jetbrains.annotations.Nullable;

public enum SkriptChatCode implements ChatCode {

    reset {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.reset = true;
        }
    },

    black("black", '0'),
    dark_blue("dark_blue", '1'),
    dark_green("dark_green", '2'),
    dark_aqua("dark_aqua", "dark_cyan", '3'),
    dark_red("dark_red", '4'),
    dark_purple("dark_purple", '5'),
    gold("gold", "orange", '6'),
    gray("gray", "light_grey", '7'),
    dark_gray("dark_gray", "dark_grey", '8'),
    blue("blue", "light_cyan", '9'),
    green("green", "light_green", 'a'),
    aqua("aqua", "light_cyan", 'b'),
    red("red", "light_red", 'c'),
    light_purple("light_purple", 'd'),
    yellow("yellow", 'e'),
    white("white", 'f'),

    bold {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.bold = true;
        }
    },

    italic {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.italic = true;
        }
    },

    underlined(null, "underline") {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.underlined = true;
        }
    },

    strikethrough {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.strikethrough = true;
        }
    },

    obfuscated(null, "magic") {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.obfuscated = true;
        }
    },

    open_url(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.clickEvent = new ClickEvent(ClickEvent.Action.open_url, param);
        }
    },

    run_command(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.clickEvent = new ClickEvent(ClickEvent.Action.run_command, param);
        }
    },

    suggest_command(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.clickEvent = new ClickEvent(ClickEvent.Action.suggest_command, param);
        }
    },

    change_page(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.clickEvent = new ClickEvent(ClickEvent.Action.change_page, param);
        }
    },

    copy_to_clipboard(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.clickEvent = new ClickEvent(ClickEvent.Action.copy_to_clipboard, param);
        }
    },

    show_text(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.hoverEvent = new HoverEvent(HoverEvent.Action.show_text, param);
        }
    },

    font(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.font = param;
        }
    },

    insertion(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.insertion = param;
        }
    },

    translate(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.translation = param;
        }
    },

    keybind(true) {
        @Override
        public void updateComponent(MessageComponent component, String param) {
            component.keybind = param;
        }
    };

    private final boolean hasParam;
    private final @Nullable String colorCode;
    private final @Nullable String langName;
    private final char colorChar;

    SkriptChatCode(@Nullable String colorCode, String langName, char colorChar) {
        this.colorCode = colorCode;
        this.langName = langName;
        this.hasParam = false;
        this.colorChar = colorChar;
    }

    SkriptChatCode(@Nullable String colorCode, String langName) {
        this.colorCode = colorCode;
        this.langName = langName;
        this.hasParam = false;
        this.colorChar = 0;
    }

    SkriptChatCode(String colorCode, char colorChar) {
        this(colorCode, colorCode, colorChar);
    }

    SkriptChatCode(boolean hasParam) {
        this.hasParam = hasParam;
        this.colorCode = null;
        this.langName = name();
        this.colorChar = 0;
    }

    SkriptChatCode() {
        this(false);
    }

    @Override
    public void updateComponent(MessageComponent component, String param) {
        component.color = colorCode;
    }

    @Override
    public boolean hasParam() {
        return hasParam;
    }

    @Override
    public @Nullable String getColorCode() {
        return colorCode;
    }

    @Override
    public @Nullable String getLangName() {
        return langName;
    }

    @Override
    public boolean isLocalized() {
        return true;
    }

    @Override
    public char getColorChar() {
        return colorChar;
    }
}
