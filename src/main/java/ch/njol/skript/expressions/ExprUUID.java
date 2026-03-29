package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("UUID")
@Description("The UUID of an offline player, entity, or world.")
@Example("set {_uuid} to uuid of player")
@Since("2.1.2, 2.2 (offline players), 2.2-dev24 (entities), 2.13.2 (fabric worlds)")
public class ExprUUID extends SimplePropertyExpression<Object, UUID> {

    static {
        register(ExprUUID.class, UUID.class, "UUID", "offlineplayers/worlds/entities");
    }

    @Override
    public @Nullable UUID convert(Object object) {
        if (object instanceof GameProfile profile) {
            return profile.id();
        }
        if (object instanceof Entity entity) {
            return entity.getUUID();
        }
        if (object instanceof ServerLevel world) {
            return UUID.nameUUIDFromBytes(world.dimension().identifier().toString().getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    @Override
    public Class<? extends UUID> getReturnType() {
        return UUID.class;
    }

    @Override
    protected String getPropertyName() {
        return "UUID";
    }
}
