package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorld extends SimplePropertyExpression<Object, ServerLevel> {

    static {
        registerDefault(ExprWorld.class, ServerLevel.class, "world", "locations/entities/chunks");
    }

    @Override
    public @Nullable ServerLevel convert(Object object) {
        if (object instanceof ServerLevel world) {
            return world;
        }
        if (object instanceof FabricLocation location) {
            return location.level();
        }
        if (object instanceof Entity entity && entity.level() instanceof ServerLevel level) {
            return level;
        }
        if (object instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel level) {
            return level;
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET
                && getExpr().isSingle()
                && ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, FabricLocation.class)) {
            return new Class[]{ServerLevel.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof ServerLevel target)) {
            return;
        }
        getExpr().changeInPlace(event, value -> {
            if (value instanceof FabricLocation location) {
                return new FabricLocation(target, location.position());
            }
            return null;
        });
    }

    @Override
    public Class<? extends ServerLevel> getReturnType() {
        return ServerLevel.class;
    }

    @Override
    protected String getPropertyName() {
        return "world";
    }
}
