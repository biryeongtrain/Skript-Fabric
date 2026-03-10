package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricDamageEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Final Damage")
@Description("How much damage is done in a damage event, considering the final amount exposed by the compatibility damage handle. Can not be changed.")
@Example("send \"%final damage%\" to victim")
@Since("2.2-dev19")
@Events("damage")
public class ExprFinalDamage extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprFinalDamage.class, Number.class, "[the] final damage");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricDamageEventHandle.class)) {
            Skript.error("The expression 'final damage' can only be used in damage events");
            return false;
        }
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricDamageEventHandle handle)) {
            return new Number[0];
        }
        return new Number[]{handle.amount()};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        Skript.error("Final damage cannot be changed; try changing the compatibility event source instead");
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the final damage";
    }
}
