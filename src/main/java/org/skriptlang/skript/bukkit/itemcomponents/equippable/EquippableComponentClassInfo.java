package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;

public final class EquippableComponentClassInfo {

    private EquippableComponentClassInfo() {
    }

    public static void register() {
        Classes.registerClassInfo(new ClassInfo<>(EquippableWrapper.class, "equippablecomponent"));
    }
}
