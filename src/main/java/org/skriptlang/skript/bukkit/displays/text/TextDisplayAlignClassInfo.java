package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;

public final class TextDisplayAlignClassInfo {

    private TextDisplayAlignClassInfo() {
    }

    public static void register() {
        ClassInfo<Display.TextDisplay.Align> info = new ClassInfo<>(Display.TextDisplay.Align.class);
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<Display.TextDisplay.Align> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable Display.TextDisplay.Align parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String normalized = input.trim().toLowerCase();
            return switch (normalized) {
                case "left", "left aligned" -> Display.TextDisplay.Align.LEFT;
                case "right", "right aligned" -> Display.TextDisplay.Align.RIGHT;
                case "center", "centered", "centre", "centred",
                        "center aligned", "centre aligned", "centered aligned", "centred aligned" ->
                        Display.TextDisplay.Align.CENTER;
                default -> null;
            };
        }
    }
}
