package org.skriptlang.skript.bukkit.displays.item;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.displays.generic.AbstractDisplayExpression;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprItemDisplayTransform extends AbstractDisplayExpression<ItemDisplayContext> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected @Nullable ItemDisplayContext convert(Display display) {
        return display instanceof Display.ItemDisplay itemDisplay ? PrivateEntityAccess.itemDisplayTransform(itemDisplay) : null;
    }

    @Override
    protected ItemDisplayContext[] createArray(int length) {
        return new ItemDisplayContext[length];
    }

    @Override
    public Class<? extends ItemDisplayContext> getReturnType() {
        return ItemDisplayContext.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{ItemDisplayContext.class, String.class};
            case RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        ItemDisplayContext transform = mode == ChangeMode.RESET ? ItemDisplayContext.NONE : resolve(delta);
        if (transform == null) {
            return;
        }
        for (var entity : displays.getAll(event)) {
            if (entity instanceof Display.ItemDisplay itemDisplay) {
                PrivateEntityAccess.setItemDisplayTransform(itemDisplay, transform);
            }
        }
    }

    private @Nullable ItemDisplayContext resolve(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return null;
        }
        Object value = delta[0];
        if (value instanceof ItemDisplayContext context) {
            return context;
        }
        if (value instanceof String string) {
            return Classes.parse(string, ItemDisplayContext.class, ParseContext.DEFAULT);
        }
        return null;
    }

    @Override
    protected String propertyName() {
        return "item display transform";
    }
}
