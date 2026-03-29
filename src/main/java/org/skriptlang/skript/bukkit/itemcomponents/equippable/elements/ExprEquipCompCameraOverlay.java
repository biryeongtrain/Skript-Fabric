package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableSupport;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEquipCompCameraOverlay extends SimpleExpression<String> {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        return values.stream(event)
                .map(EquippableSupport::getWrapper)
                .filter(java.util.Objects::nonNull)
                .map(EquippableWrapper::cameraOverlay)
                .filter(java.util.Objects::nonNull)
                .map(MinecraftResourceParser::display)
                .toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{String.class};
            case DELETE, RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Identifier overlay = null;
        if (mode == ChangeMode.SET && delta != null && delta.length > 0 && delta[0] != null) {
            overlay = MinecraftResourceParser.parse(String.valueOf(delta[0]));
        }
        for (Object value : values.getAll(event)) {
            EquippableWrapper wrapper = EquippableSupport.getWrapper(value);
            if (wrapper != null) {
                wrapper.cameraOverlay(overlay);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "camera overlay of " + values.toString(event, debug);
    }
}
