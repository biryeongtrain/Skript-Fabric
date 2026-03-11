package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.PrivateAllayAccess;

@Name("Allay Target Jukebox")
@Description("The location of the jukebox an allay is set to.")
@Example("set {_loc} to the target jukebox of last spawned allay")
@Since("2.11")
public class ExprAllayJukebox extends SimplePropertyExpression<LivingEntity, FabricLocation> {

    static {
        registerDefault(ExprAllayJukebox.class, FabricLocation.class, "target jukebox", "livingentities");
    }

    @Override
    public @Nullable FabricLocation convert(LivingEntity entity) {
        if (!(entity instanceof Allay allay) || !(allay.level() instanceof ServerLevel level)) {
            return null;
        }
        BlockPos position = PrivateAllayAccess.jukeboxPos(allay);
        return position == null ? null : new FabricLocation(level, position.getCenter());
    }

    @Override
    public Class<FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String getPropertyName() {
        return "target jukebox";
    }
}
