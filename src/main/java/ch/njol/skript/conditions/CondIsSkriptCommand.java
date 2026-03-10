package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsSkriptCommand extends Condition {

    static {
        Skript.registerCondition(
                CondIsSkriptCommand.class,
                "%strings% (is|are) [a] s(k|c)ript (command|cmd)",
                "%strings% (isn't|aren't|is not|are not) [a] s(k|c)ript (command|cmd)"
        );
    }

    private Expression<String> commands;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        commands = (Expression<String>) exprs[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (event.server() == null) {
            return isNegated();
        }
        return commands.check(event, command -> commandExists(event, command), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return commands.toString(event, debug) + " " + (isNegated() ? "is not" : "is") + " a skript command";
    }

    private static boolean commandExists(SkriptEvent event, String command) {
        String normalized = command.startsWith("/") ? command.substring(1) : command;
        int firstSpace = normalized.indexOf(' ');
        if (firstSpace >= 0) {
            normalized = normalized.substring(0, firstSpace);
        }
        if (normalized.isBlank()) {
            return false;
        }
        String label = normalized.toLowerCase(Locale.ENGLISH);
        return event.server().getCommands().getDispatcher().getRoot().getChildren().stream()
                .anyMatch(node -> node.getName().equalsIgnoreCase(label));
    }
}
