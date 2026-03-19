package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.entity.LivingEntity;

public final class LivingEntityClassInfo {

    private LivingEntityClassInfo() {
    }

    public static void register() {
        ClassInfo<LivingEntity> info = new ClassInfo<>(LivingEntity.class, "livingentity");
        Classes.registerClassInfo(info);
    }
}
