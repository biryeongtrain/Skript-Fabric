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

@Name("Damage")
@Description({
        "How much damage is done in a damage event.",
        "On this compatibility surface the event damage value is currently read-only."
})
@Example("""
	on damage:
		send "%damage%" to player
	""")
@Since("1.3.5")
@Events("damage")
public class ExprDamage extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprDamage.class, Number.class, "[the] damage");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricDamageEventHandle.class)) {
            Skript.error("The 'damage' expression may only be used in damage events");
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
        Skript.error("The damage value cannot currently be changed on this compatibility surface");
        return null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
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
        return "the damage";
    }
}
