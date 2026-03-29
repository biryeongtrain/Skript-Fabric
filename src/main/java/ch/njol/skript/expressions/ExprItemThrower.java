package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Dropped Item Thrower")
@Description("The uuid of the entity or player that threw/dropped the dropped item.")
@Example("""
	set the uuid of the dropped item thrower of {_dropped item} to player
	if the uuid of the dropped item thrower of {_dropped item} is uuid of player:
	"""
)
@Example("clear the item thrower of {_dropped item}")
@Since("2.11")
public class ExprItemThrower extends SimplePropertyExpression<ItemEntity, UUID> {

    static {
        Skript.registerExpression(ExprItemThrower.class, UUID.class,
                "[the] uuid of [the] [dropped] item thrower [of %itementities%]",
                "[the] [dropped] item thrower's uuid [of %itementities%]");
    }

    @Override
    public @Nullable UUID convert(ItemEntity item) {
        Entity thrower = item.getOwner();
        return thrower == null ? null : thrower.getUUID();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
            return new Class[]{GameProfile.class, Entity.class, UUID.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        UUID uuid = delta == null ? null : asUuid(delta[0]);
        Object[] values = ((ch.njol.skript.lang.Expression<?>) getExpr()).getArray(event);
        for (Object value : values) {
            if (!(value instanceof ItemEntity item)) {
                continue;
            }
            Entity entity = uuid == null ? null : resolveEntity(item, uuid);
            if (entity != null) {
                item.setThrower(entity);
            }
        }
    }

    private @Nullable UUID asUuid(Object value) {
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof GameProfile profile) {
            return profile.id();
        }
        if (value instanceof Entity entity) {
            return entity.getUUID();
        }
        return null;
    }

    private @Nullable Entity resolveEntity(ItemEntity item, UUID uuid) {
        if (item.level() instanceof ServerLevel level) {
            Entity levelEntity = level.getEntity(uuid);
            if (levelEntity != null) {
                return levelEntity;
            }
        }
        if (item.level().getServer() != null) {
            for (ServerLevel level : item.level().getServer().getAllLevels()) {
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    return entity;
                }
            }
        }
        return null;
    }

    @Override
    public Class<UUID> getReturnType() {
        return UUID.class;
    }

    @Override
    protected String getPropertyName() {
        return "uuid of the dropped item thrower";
    }
}
