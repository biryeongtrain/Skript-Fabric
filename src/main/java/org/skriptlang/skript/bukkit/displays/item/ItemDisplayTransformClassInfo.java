package org.skriptlang.skript.bukkit.displays.item;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;

public final class ItemDisplayTransformClassInfo {

    private ItemDisplayTransformClassInfo() {
    }

    public static void register() {
        ClassInfo<ItemDisplayContext> info = new ClassInfo<>(ItemDisplayContext.class, "itemdisplaytransform");
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<ItemDisplayContext> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable ItemDisplayContext parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String normalized = input.trim().toLowerCase().replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ");
            return switch (normalized) {
                case "none", "no transform" -> ItemDisplayContext.NONE;
                case "third person left hand", "third person left handed" -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                case "third person right hand", "third person right handed" -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                case "first person left hand", "first person left handed" -> ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                case "first person right hand", "first person right handed" -> ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                case "head" -> ItemDisplayContext.HEAD;
                case "gui" -> ItemDisplayContext.GUI;
                case "ground" -> ItemDisplayContext.GROUND;
                case "fixed" -> ItemDisplayContext.FIXED;
                default -> null;
            };
        }
    }
}
