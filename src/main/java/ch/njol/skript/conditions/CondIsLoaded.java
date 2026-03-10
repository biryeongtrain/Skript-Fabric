package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

@Name("Is Loaded")
@Description({
        "Checks whether a world, chunk or script is loaded.",
        "'chunk at 1, 1' uses chunk coordinates, which are location coords divided by 16."
})
@Example("if chunk at {home::%player's uuid%} is loaded:")
@Example("if world(\"lobby\") is loaded:")
@Example("if script named \"MyScript.sk\" is loaded:")
@Since("2.3, 2.5 (revamp with chunk at location/coords), 2.10 (Scripts)")
public class CondIsLoaded extends Condition {

    static {
        Skript.registerCondition(
                CondIsLoaded.class,
                "[chunk [at]] %locations% (is|are)[(1¦(n't| not))] loaded",
                "%scripts/worlds% (is|are)[1:(n't| not)] loaded",
                "script[s] %scripts% (is|are)[1:(n't| not)] loaded",
                "world[s] %worlds% (is|are)[1:(n't| not)] loaded"
        );
    }

    private Expression<?> objects;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        objects = exprs[0];
        setNegated(parseResult.mark == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return objects.check(event, this::isLoaded, isNegated());
    }

    private boolean isLoaded(Object value) {
        if (value instanceof FabricLocation location) {
            return location.level().hasChunk(((int) location.position().x()) >> 4, ((int) location.position().z()) >> 4);
        }
        if (value instanceof ServerLevel world) {
            return world.getServer() != null && world.getServer().getLevel(world.dimension()) != null;
        }
        if (value instanceof Script script) {
            return script.valid();
        }
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String neg = isNegated() ? " not " : " ";
        return switch (pattern) {
            case 0 -> objects.toString(event, debug) + (objects.isSingle() ? " is" : " are") + neg + "loaded";
            case 2 -> "scripts " + objects.toString(event, debug) + (objects.isSingle() ? " is" : " are") + neg + "loaded";
            case 3 -> "worlds " + objects.toString(event, debug) + (objects.isSingle() ? " is" : " are") + neg + "loaded";
            default -> objects.toString(event, debug) + (objects.isSingle() ? " is" : " are") + neg + "loaded";
        };
    }
}
