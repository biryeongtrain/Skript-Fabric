package org.skriptlang.skript.bukkit.damagesource.elements;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDamageLocation extends AbstractDamageSourceExpression<FabricLocation> {

    @Override
    protected @Nullable FabricLocation convert(SkriptEvent event, DamageSource damageSource) {
        Vec3 position = damageSource.sourcePositionRaw();
        if (position == null) {
            return null;
        }
        ServerLevel level = resolveLevel(event, damageSource);
        return new FabricLocation(level, position);
    }

    @Override
    protected FabricLocation[] createArray(int length) {
        return new FabricLocation[length];
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String propertyName() {
        return "damage location";
    }
}
