package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprSpawn extends PropertyExpression<ServerLevel, FabricLocation> {

    static {
        register(ExprSpawn.class, FabricLocation.class, "spawn[s] [(point|location)[s]]", "worlds");
    }

    @Override
    protected FabricLocation[] get(SkriptEvent event, ServerLevel[] source) {
        return get(source, world -> {
            BlockPos position = world.getSharedSpawnPos();
            return new FabricLocation(world, new Vec3(position.getX(), position.getY(), position.getZ()));
        });
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{FabricLocation.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof FabricLocation original)) {
            return;
        }
        for (ServerLevel world : getExpr().getArray(event)) {
            ServerLevel targetWorld = original.level() == null ? world : original.level();
            if (targetWorld != world) {
                continue;
            }
            world.setDefaultSpawnPos(BlockPos.containing(original.position()), 0.0F);
        }
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the spawn point of " + getExpr().toString(event, debug);
    }
}
