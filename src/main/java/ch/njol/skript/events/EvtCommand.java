package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.util.Arrays;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("unchecked")
public class EvtCommand extends SkriptEvent {

    private @Nullable String[] commands;
    private @Nullable Literal<String> commandsLiteral;

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(EvtCommand.class, "command [%-strings%]");
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtCommand.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (args.length > 0 && args[0] != null) {
            commandsLiteral = (Literal<String>) args[0];
            commands = commandsLiteral.getAll(null);
            for (int index = 0; index < commands.length; index++) {
                String command = commands[index];
                commands[index] = command.startsWith("/") ? command.substring(1) : command;
            }
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.Command handle)) {
            return false;
        }
        String command = handle.command();
        if (command.isEmpty()) {
            return false;
        }
        if (commands == null || commands.length == 0) {
            return true;
        }
        return Arrays.stream(commands).anyMatch(candidate -> matches(command, candidate));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.Command.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "command" + (commandsLiteral != null ? " " + commandsLiteral.toString(event, debug) : "");
    }

    private boolean matches(String input, String candidate) {
        String normalizedInput = input.startsWith("/") ? input.substring(1) : input;
        String normalizedCandidate = candidate.toLowerCase(Locale.ENGLISH);
        String lowercaseInput = normalizedInput.toLowerCase(Locale.ENGLISH);
        if (!lowercaseInput.startsWith(normalizedCandidate)) {
            return false;
        }
        return normalizedCandidate.contains(" ")
                || lowercaseInput.length() == normalizedCandidate.length()
                || Character.isWhitespace(lowercaseInput.charAt(normalizedCandidate.length()));
    }
}
