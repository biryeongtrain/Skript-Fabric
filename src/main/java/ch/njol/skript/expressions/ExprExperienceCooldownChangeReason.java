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

@Name("Experience Cooldown Change Reason")
@Description("The experience cooldown change reason in an experience cooldown change event.")
@Example("""
    on player experience cooldown change:
        send xp cooldown change reason to player
    """)
@Since("2.10")
public class ExprExperienceCooldownChangeReason extends SimpleExpression<String> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(
                ExprExperienceCooldownChangeReason.class,
                String.class,
                "(experience|[e]xp) cooldown change (reason|cause|type)"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.ExperienceCooldownChange.class};
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.ExperienceCooldownChange handle)
                || handle.reason() == null) {
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
        return "experience cooldown change reason";
    }
}
