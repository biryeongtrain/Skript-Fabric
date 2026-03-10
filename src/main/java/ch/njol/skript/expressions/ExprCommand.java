package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Command")
@Description("The command that caused an 'on command' event, excluding the leading slash.")
@Example("on command:\n\tbroadcast command")
@Since("2.0, Fabric")
@Events("command")
public class ExprCommand extends SimpleExpression<String> {

    private static final Class<?> COMMAND_EVENT_CLASS = commandEventClass();

    static {
        Skript.registerExpression(
                ExprCommand.class,
                String.class,
                "[the] (full|complete|whole) command",
                "[the] command [(label|alias)]"
        );
    }

    private boolean fullCommand;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(COMMAND_EVENT_CLASS)) {
            Skript.error("The command expression can only be used in command events");
            return false;
        }
        fullCommand = matchedPattern == 0;
        return true;
    }

    @Override
    protected @Nullable String[] get(SkriptEvent event) {
        String raw = readCommand(event.handle());
        if (raw == null) {
            return new String[0];
        }
        String command = normalize(raw);
        if (command.isEmpty()) {
            return new String[0];
        }
        if (fullCommand) {
            return new String[]{command};
        }
        int space = command.indexOf(' ');
        return new String[]{space == -1 ? command : command.substring(0, space)};
    }

    private String normalize(String command) {
        String normalized = command == null ? "" : command.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1).trim();
        }
        return normalized;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return fullCommand ? "the full command" : "the command";
    }

    private static Class<?> commandEventClass() {
        try {
            return Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static @Nullable String readCommand(@Nullable Object handle) {
        if (handle == null || !COMMAND_EVENT_CLASS.isInstance(handle)) {
            return null;
        }
        try {
            var method = handle.getClass().getDeclaredMethod("command");
            method.setAccessible(true);
            return (String) method.invoke(handle);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
