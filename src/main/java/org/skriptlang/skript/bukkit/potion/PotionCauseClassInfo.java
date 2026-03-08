package org.skriptlang.skript.bukkit.potion;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;

public final class PotionCauseClassInfo {

    private PotionCauseClassInfo() {
    }

    public static void register() {
        ClassInfo<FabricPotionEffectCause> info = new ClassInfo<>(FabricPotionEffectCause.class, "potioneffectcause");
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<FabricPotionEffectCause> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable FabricPotionEffectCause parse(String input, ParseContext context) {
            return FabricPotionEffectCause.parse(input);
        }
    }
}
