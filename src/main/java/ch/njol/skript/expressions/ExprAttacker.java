package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricDamageEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Attacker")
@Description({
        "The attacker of a damage event, e.g. when a player attacks a zombie this expression represents the player.",
        "Indirect sources still resolve through the compatibility damage source handle when possible."
})
@Example("""
	on damage:
		attacker is a player
	""")
@Since("1.3")
@Events("damage")
public class ExprAttacker extends SimpleExpression<Entity> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprAttacker.class, Entity.class, "[the] (attacker|damager)");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricDamageEventHandle.class};
    }

    @Override
    protected Entity @Nullable [] get(SkriptEvent event) {
        Entity attacker = getAttacker(event);
        return attacker == null ? null : new Entity[]{attacker};
    }

    static @Nullable Entity getAttacker(@Nullable SkriptEvent event) {
        if (!(event != null && event.handle() instanceof FabricDamageEventHandle handle)) {
            return null;
        }
        return handle.damageSource() == null ? null : handle.damageSource().getEntity();
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return event == null ? "the attacker" : String.valueOf(getSingle(event));
    }

    @Override
    public boolean isSingle() {
        return true;
    }
}
