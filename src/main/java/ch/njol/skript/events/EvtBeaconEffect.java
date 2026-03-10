package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public final class EvtBeaconEffect extends SkriptEvent {

    private @Nullable Literal<?> potionTypes;
    private @Nullable Boolean primaryCheck;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtBeaconEffect.class)) {
            return;
        }
        Skript.registerEvent(
                EvtBeaconEffect.class,
                "[:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
                "application of [:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
                "[:primary|:secondary] beacon effect apply [of %-potioneffecttypes%]"
        );
    }

    @Override
    public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
        potionTypes = exprs.length > 0 ? exprs[0] : null;
        if (parseResult.hasTag("primary")) {
            primaryCheck = true;
        } else if (parseResult.hasTag("secondary")) {
            primaryCheck = false;
        } else {
            primaryCheck = null;
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.BeaconEffect handle)) {
            return false;
        }
        if (primaryCheck != null && handle.primary() != primaryCheck) {
            return false;
        }
        if (potionTypes == null) {
            return true;
        }
        return potionTypes.check(event, type -> Objects.equals(normalize(type), normalize(handle.effectType())));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.BeaconEffect.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return (primaryCheck == null ? "" : primaryCheck ? "primary " : "secondary ")
                + "beacon effect"
                + (potionTypes == null ? "" : " of " + potionTypes.toString(event, debug));
    }

    private static @Nullable String normalize(@Nullable Object value) {
        return value == null ? null : value.toString().toLowerCase(java.util.Locale.ENGLISH);
    }
}
