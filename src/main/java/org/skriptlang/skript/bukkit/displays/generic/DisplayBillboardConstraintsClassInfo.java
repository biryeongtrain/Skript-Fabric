package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;

public final class DisplayBillboardConstraintsClassInfo {

    private DisplayBillboardConstraintsClassInfo() {
    }

    public static void register() {
        ClassInfo<Display.BillboardConstraints> info = new ClassInfo<>(Display.BillboardConstraints.class, "billboardconstraints");
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<Display.BillboardConstraints> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable Display.BillboardConstraints parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String normalized = input.trim().toLowerCase();
            return switch (normalized) {
                case "fixed" -> Display.BillboardConstraints.FIXED;
                case "vertical", "vertical billboard", "vertical billboarding" -> Display.BillboardConstraints.VERTICAL;
                case "horizontal", "horizontal billboard", "horizontal billboarding" -> Display.BillboardConstraints.HORIZONTAL;
                case "center", "centered", "centre", "centred",
                        "center billboard", "centre billboard", "center billboarding", "centre billboarding" -> Display.BillboardConstraints.CENTER;
                default -> null;
            };
        }
    }
}
