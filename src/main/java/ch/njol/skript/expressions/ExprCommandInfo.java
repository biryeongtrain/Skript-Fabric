package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprCommandInfo extends SimpleExpression<String> {

    private enum InfoType {
        NAME(CommandNode::getName),
        DESCRIPTION(node -> {
            ScriptCommand sc = Commands.getCommand(node.getName());
            return sc != null ? sc.getDescription() : null;
        }),
        LABEL(CommandNode::getName),
        USAGE(node -> {
            ScriptCommand sc = Commands.getCommand(node.getName());
            return sc != null && sc.getUsage() != null ? sc.getUsage() : "/" + node.getUsageText();
        }),
        ALIASES(node -> null), // handled separately in get()
        PERMISSION(node -> {
            ScriptCommand sc = Commands.getCommand(node.getName());
            return sc != null ? sc.getPermission() : null;
        }),
        PERMISSION_MESSAGE(node -> {
            ScriptCommand sc = Commands.getCommand(node.getName());
            return sc != null ? sc.getPermissionMessage() : null;
        }),
        PLUGIN(node -> {
            ScriptCommand sc = Commands.getCommand(node.getName());
            return sc != null ? "Skript" : null;
        });

        private final Function<CommandNode<?>, @Nullable String> reader;

        InfoType(Function<CommandNode<?>, @Nullable String> reader) {
            this.reader = reader;
        }
    }

    private static final Class<?> COMMAND_EVENT_CLASS = commandEventClass();

    static {
        Skript.registerExpression(ExprCommandInfo.class, String.class,
                "[the] main command [label|name] [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] main command [label|name]",
                "[the] description [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] description",
                "[the] label [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] label",
                "[the] usage [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] usage",
                "[(all|the|all [of] the)] aliases [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] aliases",
                "[the] permission [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] permission",
                "[the] permission message [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] permission message",
                "[the] plugin [owner] [of [[the] command[s] %-strings%]]",
                "command[s] %strings%'[s] plugin [owner]");
    }

    private InfoType type = InfoType.NAME;
    private @Nullable Expression<String> commandName;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        commandName = (Expression<String>) exprs[0];
        if (commandName == null && !getParser().isCurrentEvent(COMMAND_EVENT_CLASS)) {
            Skript.error("The command info expression requires a command input outside of command events");
            return false;
        }
        type = InfoType.values()[Math.floorDiv(matchedPattern, 2)];
        return true;
    }

    @Override
    protected String[] get(SkriptEvent event) {
        List<CommandNode<?>> commands = resolveCommands(event);
        if (commands.isEmpty()) {
            return new String[0];
        }
        if (type == InfoType.ALIASES) {
            List<String> aliases = new ArrayList<>();
            for (CommandNode<?> command : commands) {
                aliases.addAll(resolveAliases(event, command));
            }
            return aliases.stream()
                    .filter(alias -> alias != null && !alias.isBlank())
                    .distinct()
                    .toArray(String[]::new);
        }
        return commands.stream()
                .map(type.reader)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .toArray(String[]::new);
    }

    private List<CommandNode<?>> resolveCommands(SkriptEvent event) {
        if (event.server() == null) {
            return List.of();
        }
        Collection<? extends CommandNode<?>> rootChildren = event.server().getCommands().getDispatcher().getRoot().getChildren();
        if (commandName != null) {
            List<CommandNode<?>> nodes = new ArrayList<>();
            commandName.stream(event)
                    .map(ExprCommandInfo::normalizeCommandName)
                    .filter(name -> !name.isBlank())
                    .forEach(name -> rootChildren.stream()
                            .filter(node -> node.getName().equals(name))
                            .findFirst()
                            .ifPresent(nodes::add));
            return nodes;
        }
        String raw = readCommand(event.handle());
        if (raw == null) {
            return List.of();
        }
        String name = normalizeCommandName(raw);
        if (name.isBlank()) {
            return List.of();
        }
        return rootChildren.stream()
                .filter(node -> node.getName().equals(name))
                .map(node -> (CommandNode<?>) node)
                .findFirst()
                .<List<CommandNode<?>>>map(node -> List.of((CommandNode<?>) node))
                .orElseGet(List::of);
    }

    private List<String> resolveAliases(SkriptEvent event, CommandNode<?> command) {
        // Try ScriptCommand aliases first
        ScriptCommand sc = Commands.getCommand(command.getName());
        if (sc != null) {
            return sc.getAliases();
        }
        if (event.server() == null) {
            return List.of();
        }
        List<String> aliases = new ArrayList<>();
        Object targetCommand = command.getCommand();
        Object redirect = command.getRedirect();
        for (CommandNode<?> candidate : event.server().getCommands().getDispatcher().getRoot().getChildren()) {
            if (candidate == command) {
                continue;
            }
            if (candidate.getCommand() == targetCommand && candidate.getRedirect() == redirect) {
                aliases.add(candidate.getName());
            }
        }
        return aliases;
    }

    private static String normalizeCommandName(String input) {
        String normalized = input == null ? "" : input.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1).trim();
        }
        int space = normalized.indexOf(' ');
        if (space >= 0) {
            normalized = normalized.substring(0, space);
        }
        int colon = normalized.lastIndexOf(':');
        if (colon >= 0 && colon + 1 < normalized.length()) {
            normalized = normalized.substring(colon + 1);
        }
        return normalized;
    }

    @Override
    public boolean isSingle() {
        return type != InfoType.ALIASES && (commandName == null || commandName.isSingle());
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the " + type.name().toLowerCase(Locale.ENGLISH).replace('_', ' ')
                + (commandName == null ? "" : " of command " + commandName.toString(event, debug));
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
