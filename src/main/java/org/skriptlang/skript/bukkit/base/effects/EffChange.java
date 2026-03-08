package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyReceiverExpression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffChange extends Effect {

    private ChangeMode mode;
    private Expression<?> changed;
    private @Nullable Expression<?> changeWith;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        switch (matchedPattern) {
            case 0 -> {
                mode = ChangeMode.SET;
                changed = expressions[0];
                changeWith = expressions[1];
            }
            case 1 -> {
                mode = ChangeMode.ADD;
                changeWith = expressions[0];
                changed = expressions[1];
            }
            case 2 -> {
                mode = ChangeMode.REMOVE;
                changeWith = expressions[0];
                changed = expressions[1];
            }
            case 3 -> {
                mode = ChangeMode.RESET;
                changed = expressions[0];
                changeWith = null;
            }
            case 4 -> {
                mode = ChangeMode.DELETE;
                changed = expressions[0];
                changeWith = null;
            }
            default -> {
                return false;
            }
        }
        return changed.acceptChange(mode) != null;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object[] delta = null;
        if (changeWith != null) {
            delta = resolveDelta(event);
            if (delta == null || delta.length == 0) {
                if (mode == ChangeMode.SET && changed.acceptChange(ChangeMode.DELETE) != null) {
                    changed.change(event, null, ChangeMode.DELETE);
                }
                return;
            }
            if (supportsKeyedChange(mode)
                    && changed instanceof KeyReceiverExpression<?> receiver
                    && KeyProviderExpression.areKeysRecommended(changeWith)) {
                receiver.change(event, delta, mode, ((KeyProviderExpression<?>) changeWith).getArrayKeys(event));
                return;
            }
        }
        changed.change(event, delta, mode);
    }

    private @Nullable Object[] resolveDelta(SkriptEvent event) {
        assert changeWith != null;
        Expression<?> effectiveChange = changeWith;
        Class<?>[] acceptedTypes = changed.acceptChange(mode);
        Expression<?> source = effectiveChange.getSource();
        if (acceptedTypes != null && acceptedTypes.length > 0 && source instanceof UnparsedLiteral unparsedLiteral) {
            Literal<?> reparsed = unparsedLiteral.getConvertedExpression(ParseContext.DEFAULT, acceptedTypes);
            if (reparsed != null) {
                effectiveChange = reparsed;
            }
        }
        Object[] delta = effectiveChange.getArray(event);
        return effectiveChange.beforeChange(changed, delta);
    }

    private static boolean supportsKeyedChange(ChangeMode mode) {
        return mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (mode) {
            case SET -> "set " + changed.toString(event, debug) + " to "
                    + (changeWith == null ? "" : changeWith.toString(event, debug));
            case ADD -> "add " + (changeWith == null ? "" : changeWith.toString(event, debug)) + " to "
                    + changed.toString(event, debug);
            case REMOVE -> "remove " + (changeWith == null ? "" : changeWith.toString(event, debug)) + " from "
                    + changed.toString(event, debug);
            case RESET -> "reset " + changed.toString(event, debug);
            case DELETE -> "delete " + changed.toString(event, debug);
        };
    }
}
