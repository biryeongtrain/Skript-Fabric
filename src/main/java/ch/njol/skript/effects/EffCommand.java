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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Command")
@Description({
        "Executes a command. This can be useful to use other plugins in triggers.",
        "The bungeecord command option is not available in the Fabric runtime."
})
@Example("make player execute command \"/home\"")
@Example("execute console command \"/say Hello everyone!\"")
@Since("1.0, 2.8.0 (bungeecord command)")
public final class EffCommand extends Effect {

    private static boolean registered;

    private @Nullable Expression<ServerPlayer> senders;
    private Expression<String> commands;
    private boolean bungeecord;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffCommand.class,
                "[execute] [the] [bungee:bungee[cord]] command[s] %strings% [by %-players%]",
                "[execute] [the] %players% [bungee:bungee[cord]] command[s] %strings%",
                "(let|make) %players% execute [[the] [bungee:bungee[cord]] command[s]] %strings%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (matchedPattern == 0) {
            commands = (Expression<String>) exprs[0];
            senders = (Expression<ServerPlayer>) exprs[1];
        } else {
            senders = (Expression<ServerPlayer>) exprs[0];
            commands = (Expression<String>) exprs[1];
        }
        bungeecord = parseResult.hasTag("bungee");
        if (bungeecord) {
            Skript.error("The bungeecord command option is not available in the Fabric runtime");
            return false;
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.server() == null) {
            return;
        }
        for (String raw : commands.getArray(event)) {
            String command = raw.startsWith("/") ? raw.substring(1) : raw;
            if (senders != null) {
                for (ServerPlayer sender : senders.getArray(event)) {
                    CommandSourceStack source = sender.createCommandSourceStack();
                    event.server().getCommands().performPrefixedCommand(source, command);
                }
            } else {
                event.server().getCommands().performPrefixedCommand(event.server().createCommandSourceStack(), command);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + (senders != null ? senders.toString(event, debug) : "the console")
                + " execute command " + commands.toString(event, debug);
    }
}
