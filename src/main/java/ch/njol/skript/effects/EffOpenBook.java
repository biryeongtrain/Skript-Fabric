package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Open Book")
@Description("Opens a written book to a player.")
@Example("open book player's tool to player")
@RequiredPlugins("Minecraft 1.14.2+")
@Since("2.5.1")
public final class EffOpenBook extends Effect {

    private static boolean registered;

    private Expression<FabricItemType> book;
    private Expression<ServerPlayer> players;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffOpenBook.class, "(open|show) book %itemtype% (to|for) %players%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        book = (Expression<FabricItemType>) exprs[0];
        players = (Expression<ServerPlayer>) exprs[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricItemType itemType = book.getSingle(event);
        if (itemType == null) {
            return;
        }
        var stack = itemType.toStack();
        if (!stack.is(Items.WRITTEN_BOOK)) {
            return;
        }
        for (ServerPlayer player : players.getArray(event)) {
            player.openItemGui(stack, InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "open book " + book.toString(event, debug) + " to " + players.toString(event, debug);
    }
}
