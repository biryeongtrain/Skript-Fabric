package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Do If")
@Description("Execute an effect if a condition is true.")
@Example("""
        on join:
            give a diamond to the player if the player has permission "rank.vip"
        """)
@Since("2.3")
public class EffDoIf extends Effect {

    private static boolean registered;

    private Effect effect;
    private Condition condition;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffDoIf.class, "<.+> if <.+>");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        String eff = parseResult.regexes.get(0).group();
        String cond = parseResult.regexes.get(1).group();
        effect = Effect.parse(eff, "Can't understand this effect: " + eff);
        if (effect instanceof EffDoIf) {
            Skript.error("Do if effects may not be nested!");
            return false;
        }
        condition = Condition.parse(cond, "Can't understand this condition: " + cond);
        if (effect == null || condition == null) {
            return false;
        }
        if (effect.loaderExecutionIntent() instanceof ExecutionIntent.StopSections intent) {
            getParser().getHintManager().mergeScope(0, intent.levels(), true);
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public @Nullable TriggerItem walk(SkriptEvent event) {
        if (condition.check(event)) {
            effect.setParent(getParent());
            effect.setNext(getNext());
            return effect;
        }
        return getNext();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return effect.toString(event, debug) + " if " + condition.toString(event, debug);
    }
}
