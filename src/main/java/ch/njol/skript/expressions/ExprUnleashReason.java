package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
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
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Unleash Reason")
@Description("The unleash reason in an unleash event.")
@Events("Leash / Unleash")
@Example("send unleash reason to player")
@Since("2.10")
public class ExprUnleashReason extends SimpleExpression<String> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprUnleashReason.class, String.class, "[the] unleash[ing] reason");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.Leash.class};
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Leash handle)) {
            return null;
        }
        return switch (handle.action()) {
            case LEASH -> null;
            case UNLEASH, PLAYER_UNLEASH -> new String[]{handle.action().name().toLowerCase(Locale.ENGLISH)};
        };
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
        return "unleash reason";
    }
}
