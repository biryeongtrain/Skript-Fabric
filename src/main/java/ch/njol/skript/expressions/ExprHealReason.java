package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
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

@Name("Heal Reason")
@Description("The heal reason of a heal event.")
@Example("""
	on heal:
		send heal reason to player
	""")
@Events("heal")
@Since("2.5")
public class ExprHealReason extends SimpleExpression<String> implements EventRestrictedSyntax {

    static {
        ch.njol.skript.Skript.registerExpression(ExprHealReason.class, String.class, "[the] (regen|health regain|heal[ing]) (reason|cause)");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.Healing.class};
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Healing handle) || handle.reason() == null) {
            return null;
        }
        return new String[]{handle.reason()};
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
        return "heal reason";
    }
}
