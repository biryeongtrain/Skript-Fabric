package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

/**
 * @author Peter Guttiger
 */
@Name("Head location")
@Description({"The location of an entity's head, mostly useful for players and e.g. looping blocks in the player's line of sight.",
        "Please note that this location is only accurate for entities whose head is exactly above their center, i.e. players, endermen, zombies, skeletons, etc., but not sheep, pigs or cows."})
@Example("set the block at the player's head to air")
@Example("set the block in front of the player's eyes to glass")
@Example("loop blocks in front of the player's head:")
@Since("2.0")
public class ExprEyeLocation extends SimplePropertyExpression<LivingEntity, FabricLocation> {

    static {
        register(ExprEyeLocation.class, FabricLocation.class, "(head|eye[s]) [location[s]]", "livingentities");
    }

    @Override
    public Class<FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String getPropertyName() {
        return "eye location";
    }

    @Override
    public @Nullable FabricLocation convert(LivingEntity entity) {
        return entity.level() instanceof ServerLevel level ? new FabricLocation(level, entity.getEyePosition()) : null;
    }
}
