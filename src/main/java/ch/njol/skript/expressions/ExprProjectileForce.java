package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Projectile Force")
@Description("Returns the force at which a projectile was shot within an entity shoot bow event.")
@Example("""
    on skeleton shoot bow:
        if projectile force > 0.9:
            set test block at 4 1 0 to emerald_block
    """)
@Since("2.11, Fabric")
public class ExprProjectileForce extends SimpleExpression<Float> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprProjectileForce.class, Float.class, "[the] projectile force");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.EntityShootBow.class};
    }

    @Override
    protected Float @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.EntityShootBow handle)) {
            return null;
        }
        return new Float[]{handle.force()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "projectile force";
    }
}
