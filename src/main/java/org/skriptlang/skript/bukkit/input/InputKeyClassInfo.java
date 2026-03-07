package org.skriptlang.skript.bukkit.input;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

public final class InputKeyClassInfo {

    private InputKeyClassInfo() {
    }

    public static void register() {
        ClassInfo<InputKey> info = new ClassInfo<>(InputKey.class);
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<InputKey> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable InputKey parse(String input, ParseContext context) {
            return InputKey.parse(input);
        }
    }
}
