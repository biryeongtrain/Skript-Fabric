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
import java.lang.reflect.Field;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

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
        try {
            Field ownerField = ItemEntity.class.getDeclaredField("owner");
            ownerField.setAccessible(true);
            return (UUID) ownerField.get(item);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
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
        for (ItemEntity item : getExpr().getArray(event)) {
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
        for (java.lang.reflect.Method method : ItemEntity.class.getMethods()) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == UUID.class) {
                try {
                    method.setAccessible(true);
                    method.invoke(item, uuid);
                } catch (ReflectiveOperationException ignored) {
                }
                return;
            }
        }
        try {
            Field ownerField = ItemEntity.class.getDeclaredField("owner");
            ownerField.setAccessible(true);
            ownerField.set(item, uuid);
        } catch (ReflectiveOperationException ignored) {
        }
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
