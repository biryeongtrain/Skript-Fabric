package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerCommandSend extends SkriptEvent {

    private final Collection<String> originalCommands = new java.util.ArrayList<>();

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(
                EvtPlayerCommandSend.class,
                "send[ing] [of [the]] [server] command[s] list",
                "[server] command list send"
        );
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtPlayerCommandSend.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.CommandSend handle)) {
            return false;
        }
        originalCommands.clear();
        originalCommands.addAll(handle.snapshot());
        return true;
    }

    public List<String> getOriginalCommands() {
        return List.copyOf(originalCommands);
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.CommandSend.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "sending of the server command list";
    }
}
