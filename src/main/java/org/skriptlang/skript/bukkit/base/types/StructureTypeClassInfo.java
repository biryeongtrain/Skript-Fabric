package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.StructureType;

public final class StructureTypeClassInfo {

    private StructureTypeClassInfo() {}

    public static void register() {
        ClassInfo<StructureType> info = new ClassInfo<>(StructureType.class, "structuretype");
        info.user("structure ?types?|tree ?types?");
        info.parser(new Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
            }

            @Override
            public @Nullable StructureType parse(String input, ParseContext context) {
                if (input == null || input.isBlank()) return null;
                return StructureType.fromName(input.trim());
            }

            @Override
            public String toString(StructureType type, int flags) {
                return type.name();
            }

            @Override
            public String toVariableNameString(StructureType type) {
                return type.name();
            }
        });
        Classes.registerClassInfo(info);
    }
}
