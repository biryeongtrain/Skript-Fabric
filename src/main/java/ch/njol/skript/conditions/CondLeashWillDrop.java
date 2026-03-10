package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Leash Will Drop")
@Description("Checks whether the leash item will drop during the leash detaching in an unleash event.")
@Example("""
    on unleash:
        if the leash will drop:
            prevent the leash from dropping
        else:
            allow the leash to drop
    """)
@Keywords("lead")
@Events("Leash / Unleash")
@Since("2.10")
public class CondLeashWillDrop extends Condition {

    private static final @Nullable Class<?> ENTITY_UNLEASH_EVENT = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$EntityUnleash");

    static {
        Skript.registerCondition(CondLeashWillDrop.class, "[the] (lead|leash) [item] (will|not:(won't|will not)) (drop|be dropped)");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (ENTITY_UNLEASH_EVENT == null || !getParser().isCurrentEvent(ENTITY_UNLEASH_EVENT)) {
            Skript.error("The 'leash will drop' condition can only be used in an 'unleash' event");
            return false;
        }
        setNegated(parseResult.hasTag("not"));
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return ConditionRuntimeSupport.booleanMethod(event.handle(), false, "isDropLeash", "dropLeash") ^ isNegated();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the leash will" + (isNegated() ? " not" : "") + " be dropped";
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
