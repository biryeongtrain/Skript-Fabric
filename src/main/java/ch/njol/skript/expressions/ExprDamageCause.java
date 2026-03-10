package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceTypeSupport;
import org.skriptlang.skript.fabric.runtime.FabricDamageSourceEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Damage Cause")
@Description("The damage cause of a damage event.")
@Example("send damage cause to player")
@Since("2.0")
public class ExprDamageCause extends SimpleExpression<String> implements EventRestrictedSyntax {

    static {
        register();
    }

    private static void register() {
        ch.njol.skript.Skript.registerExpression(ExprDamageCause.class, String.class, "[the] damage cause");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricDamageSourceEventHandle.class};
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricDamageSourceEventHandle handle) || handle.damageSource() == null) {
            return null;
        }
        return new String[]{DamageSourceTypeSupport.display(handle.damageSource())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "damage cause";
    }
}
