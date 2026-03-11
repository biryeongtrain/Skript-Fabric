package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateItemEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;
import java.util.UUID;

@Name("Dropped Item Owner")
@Description("""
	The uuid of the owner of the dropped item.
	Setting the owner of a dropped item means only that entity or player can pick it up.
	Dropping an item does not automatically make the entity or player the owner.
	""")
@Example("""
		set the uuid of the dropped item owner of last dropped item to player
		if the uuid of the dropped item owner of last dropped item is uuid of player:
	""")
@Since("2.11")
public class ExprItemOwner extends SimplePropertyExpression<ItemEntity, UUID> {

    static {
        Skript.registerExpression(ExprItemOwner.class, UUID.class,
                "[the] uuid of [the] [dropped] item owner [of %itementities%]",
                "[the] [dropped] item owner's uuid [of %itementities%]");
    }

    @Override
    public @Nullable UUID convert(ItemEntity item) {
        return PrivateItemEntityAccess.owner(item);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
            return new Class[]{Entity.class, GameProfile.class, UUID.class};
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
            setOwner(item, uuid);
        }
    }

    private @Nullable UUID asUuid(Object value) {
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof GameProfile profile) {
            return profile.getId();
        }
        if (value instanceof Entity entity) {
            return entity.getUUID();
        }
        return null;
    }

    private void setOwner(ItemEntity item, @Nullable UUID uuid) {
        PrivateItemEntityAccess.setOwner(item, uuid);
    }

    @Override
    public Class<? extends UUID> getReturnType() {
        return UUID.class;
    }

    @Override
    protected String getPropertyName() {
        return "uuid of the dropped item owner";
    }
}
