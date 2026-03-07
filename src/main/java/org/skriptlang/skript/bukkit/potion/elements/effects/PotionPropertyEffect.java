package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public abstract class PotionPropertyEffect extends Effect {

    public enum Type {
        MAKE,
        SHOW
    }

    public static String[] getPatterns(Type type, String property) {
        return switch (type) {
            case MAKE -> new String[]{
                    "make %objects% " + property,
                    "make %objects% not " + property
            };
            case SHOW -> new String[]{
                    "show [the] [potion] " + property + " of %objects%",
                    "hide [the] [potion] " + property + " of %objects%",
                    "show [the] [potion] " + property + " for %objects%",
                    "hide [the] [potion] " + property + " for %objects%",
                    "show [the] [potion] " + property,
                    "hide [the] [potion] " + property,
                    "show %objects%'[s] " + property,
                    "hide %objects%'[s] " + property
            };
        };
    }

    private Expression<?> potions;
    private boolean negated;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length < 0 || expressions.length > 1) {
            return false;
        }
        if ((matchedPattern >= 4 && matchedPattern <= 5) && expressions.length != 0) {
            return false;
        }
        if (!(matchedPattern >= 4 && matchedPattern <= 5) && expressions.length != 1) {
            return false;
        }
        potions = matchedPattern >= 4 && matchedPattern <= 5 ? null : expressions[0];
        negated = switch (matchedPattern) {
            case 1, 3, 5, 7 -> true;
            default -> false;
        };
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object[] values;
        if (potions != null) {
            values = potions.getAll(event);
        } else if (event.handle() instanceof SkriptPotionEffect effect) {
            values = new Object[]{effect};
        } else {
            values = new Object[0];
        }
        for (Object value : values) {
            SkriptPotionEffect effect = PotionEffectSupport.parsePotionEffect(value);
            if (effect != null) {
                modify(effect, negated);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getPropertyType().name().toLowerCase(java.util.Locale.ENGLISH) + " " + getPropertyName();
    }

    public abstract void modify(SkriptPotionEffect effect, boolean isNegated);

    public abstract Type getPropertyType();

    public abstract String getPropertyName();
}
