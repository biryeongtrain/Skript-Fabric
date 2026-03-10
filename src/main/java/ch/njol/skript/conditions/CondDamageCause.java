package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceTypeSupport;
import org.skriptlang.skript.fabric.runtime.FabricDamageSourceEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Damage Cause")
@Description("Tests what kind of damage caused a damage event.")
@Example("""
    on damage:
        damage was caused by lava, fire or burning
        victim is a player
    """)
@Since("2.0")
public class CondDamageCause extends Condition {

    static {
        Skript.registerCondition(CondDamageCause.class,
                "[the] damage (was|is|has)(0¦|1¦n('|o)t) [been] (caused|done|made) by %strings%");
    }

    private Expression<String> expected;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        expected = (Expression<String>) exprs[0];
        setNegated(parseResult.mark == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (!(event.handle() instanceof FabricDamageSourceEventHandle handle) || handle.damageSource() == null) {
            return false;
        }
        String cause = ConditionRuntimeSupport.normalizeToken(DamageSourceTypeSupport.display(handle.damageSource()));
        return expected.check(event, input -> matchesCause(cause, input), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "damage was" + (isNegated() ? " not" : "") + " caused by " + expected.toString(event, debug);
    }

    private boolean matchesCause(String cause, String input) {
        String normalized = ConditionRuntimeSupport.normalizeToken(input);
        if (cause.equals(normalized)) {
            return true;
        }
        return switch (normalized) {
            case "fire", "burning", "on fire", "in fire" -> cause.equals("in fire") || cause.equals("on fire");
            default -> false;
        };
    }
}
