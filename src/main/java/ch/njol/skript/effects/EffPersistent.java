package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Persistent")
@Description({
        "Make entities, players, or leaves be persistent.",
        "Persistence of entities is whether they are retained through server restarts.",
        "Persistence of leaves is whether they should decay when not connected to a log block within 6 meters.",
        "Persistence of players is if the player's playerdata should be saved when they leave the server. "
                + "Players' persistence is reset back to 'true' when they join the server.",
        "Passengers inherit the persistence of their vehicle, meaning a persistent zombie put on a "
                + "non-persistent chicken will become non-persistent. This does not apply to players.",
        "By default, all entities are persistent."
})
@Example("prevent all entities from persisting")
@Example("force {_leaves} to persist")
@Example("""
        command /kickcheater <cheater: player>:
            permission: op
            trigger:
                prevent {_cheater} from persisting
                kick {_cheater}
        """)
@Since("2.11")
public class EffPersistent extends Effect {

    private static boolean registered;

    private Expression<?> source;
    private boolean persist;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffPersistent.class,
                "make %entities/blocks% [:not] persist[ent]",
                "force %entities/blocks% to [:not] persist",
                "prevent %entities/blocks% from persisting"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        source = exprs[0];
        persist = matchedPattern < 2 && !parseResult.hasTag("not");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Object object : source.getArray(event)) {
            if (object instanceof Mob mob) {
                if (persist) {
                    mob.setPersistenceRequired();
                } else {
                    EffectRuntimeSupport.setBooleanField(mob, "persistenceRequired", false);
                }
            } else if (object instanceof Entity entity) {
                EffectRuntimeSupport.setBooleanField(entity, "persistenceRequired", persist);
            } else if (object instanceof FabricBlock block) {
                BlockState state = block.state();
                if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
                    block.level().setBlock(block.position(), state.setValue(BlockStateProperties.PERSISTENT, persist), 3);
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (persist) {
            return "make " + source.toString(event, debug) + " persistent";
        }
        return "prevent " + source.toString(event, debug) + " from persisting";
    }
}
