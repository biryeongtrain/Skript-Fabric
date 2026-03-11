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

@Name("Transform Reason")
@Description("The transform reason within an entity transform event.")
@Example("send transform reason to player")
@Since("2.8.0")
public class ExprTransformReason extends SimpleExpression<String> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprTransformReason.class, String.class, "[the] transform[ing] (cause|reason|type)");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.EntityTransform.class};
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.EntityTransform handle) || handle.reason() == null) {
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
        return "transform reason";
    }
}
