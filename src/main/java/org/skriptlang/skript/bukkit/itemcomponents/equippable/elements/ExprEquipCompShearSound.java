package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableSupport;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEquipCompShearSound extends SimpleExpression<String> {

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
                .map(EquippableWrapper::shearSound)
                .map(Holder::unwrapKey)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(key -> MinecraftResourceParser.display(key.identifier()))
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
        Holder<SoundEvent> sound = null;
        if (mode == ChangeMode.SET && delta != null && delta.length > 0 && delta[0] != null) {
            SoundEvent eventSound = BuiltInRegistries.SOUND_EVENT.getValue(MinecraftResourceParser.parse(String.valueOf(delta[0])));
            if (eventSound != null) {
                sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(eventSound);
            }
        }
        for (Object value : values.getAll(event)) {
            EquippableWrapper wrapper = EquippableSupport.getWrapper(value);
            if (wrapper != null && sound != null) {
                wrapper.shearSound(sound);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "shear sound of " + values.toString(event, debug);
    }
}
