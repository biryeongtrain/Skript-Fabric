package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.StructureType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Tree")
@Description({
        "Creates a tree.",
        "This may require that there is enough space above the given location and that the block below is dirt/grass."
})
@Example("grow a tall redwood tree above the clicked block")
@Since("1.0")
public class EffTree extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffTree.class,
                "(grow|create|generate) tree [of type %structuretype%] %directions% %locations%",
                "(grow|create|generate) %structuretype% %directions% %locations%"
        );
        registered = true;
    }

    @Nullable
    private Expression<StructureType> treeType;
    private Expression<FabricLocation> locations;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        treeType = (Expression<StructureType>) exprs[0];
        locations = Direction.combine(
                (Expression<? extends Direction>) exprs[1],
                (Expression<? extends FabricLocation>) exprs[2]
        );
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        StructureType type;
        if (treeType != null) {
            type = treeType.getSingle(event);
        } else {
            type = StructureType.fromName("oak");
        }
        if (type == null) {
            type = StructureType.fromName("oak");
        }
        if (type == null) {
            return;
        }
        for (FabricLocation loc : locations.getArray(event)) {
            if (loc.level() instanceof ServerLevel serverLevel) {
                type.place(serverLevel, BlockPos.containing(loc.position()));
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "grow " + (treeType != null ? treeType.toString(event, debug) + " " : "tree ") + locations.toString(event, debug);
    }
}
