package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Bed/Anchor Spawn")
@Description("Checks what the respawn location of a player in the respawn event is.")
@Example("""
    on respawn:
        the respawn location is a bed
        broadcast "%player% is respawning in their bed! So cozy!"
    """)
@RequiredPlugins("Minecraft 1.16+")
@Since("2.7")
@Events("respawn")
public class CondRespawnLocation extends Condition {

    private static final @Nullable Class<?> PLAYER_RESPAWN_EVENT = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn");

    static {
        Skript.registerCondition(CondRespawnLocation.class, "[the] respawn location (was|is)[1:(n'| no)t] [a] (:bed|respawn anchor)");
    }

    private boolean bedSpawn;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (PLAYER_RESPAWN_EVENT == null || !getParser().isCurrentEvent(PLAYER_RESPAWN_EVENT)) {
            Skript.error("The 'respawn location' condition may only be used in a respawn event");
            return false;
        }
        setNegated(parseResult.mark == 1);
        bedSpawn = parseResult.hasTag("bed");
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        boolean match = bedSpawn
                ? ConditionRuntimeSupport.booleanMethod(event.handle(), false, "isBedSpawn", "bedSpawn")
                : ConditionRuntimeSupport.booleanMethod(event.handle(), false, "isAnchorSpawn", "anchorSpawn");
        return match != isNegated();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the respawn location " + (isNegated() ? "isn't" : "is") + " a " + (bedSpawn ? "bed spawn" : "respawn anchor");
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
