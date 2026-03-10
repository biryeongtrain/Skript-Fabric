package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Block Break Speed")
@Description(
        "Gets the speed at which the given player would break this block, taking into account tools, potion effects, " +
                "whether or not the player is in water, enchantments, etc. The returned value is the amount of progress made in " +
                "breaking the block each tick. When the total breaking progress reaches 1.0, the block is broken. Note that the " +
                "break speed can change in the course of breaking a block, e.g. if a potion effect is applied or expires, or the " +
                "player jumps/enters water."
)
@Example("""
    on left click using diamond pickaxe:
        event-block is set
        send "Break Speed: %break speed for player%" to player
    """)
@Since("2.7")
@RequiredPlugins("Minecraft 1.21+")
public class ExprBreakSpeed extends SimpleExpression<Float> {

    static {
        Skript.registerExpression(ExprBreakSpeed.class, Float.class,
                "[the] break speed[s] [of %blocks%] [for %players%]",
                "%block%'[s] break speed[s] [for %players%]");
    }

    private Expression<FabricBlock> blocks;
    private Expression<ServerPlayer> players;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        blocks = (Expression<FabricBlock>) exprs[0];
        players = (Expression<ServerPlayer>) exprs[1];
        return true;
    }

    @Override
    protected Float @Nullable [] get(SkriptEvent event) {
        List<Float> speeds = new ArrayList<>();
        for (FabricBlock block : blocks.getArray(event)) {
            for (ServerPlayer player : players.getArray(event)) {
                speeds.add(player.getDestroySpeed(block.state()));
            }
        }
        return speeds.toArray(Float[]::new);
    }

    @Override
    public boolean isSingle() {
        return blocks.isSingle() && players.isSingle();
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "break speed of " + blocks.toString(event, debug) + " for " + players.toString(event, debug);
    }
}
