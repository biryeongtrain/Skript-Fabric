package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.command.ScriptCommandContext;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.regex.MatchResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Argument")
@Description({
        "Usable in command events. Holds the arguments given to the command after the command label.",
        "For example, if \"/tell Njol Hello\" is used, argument 1 is \"Njol\" and the last argument is \"Hello\"."
})
@Example("on command:\n\tbroadcast argument 1")
@Since("1.0, 2.7 (command events), Fabric")
public class ExprArgument extends SimpleExpression<String> {

    private static final Class<?> COMMAND_EVENT_CLASS = commandEventClass();
    private static final int LAST = 0;
    private static final int ORDINAL = 1;
    private static final int SINGLE = 2;
    private static final int ALL = 3;

    static {
        Skript.registerExpression(ExprArgument.class, String.class,
                "[the] last arg[ument]",
                "[the] arg[ument](-| )<(\\d+)>",
                "[the] <(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th> arg[ument][s]",
                "[(all [[of] the]|the)] arg[ument][(1:s)]"
        );
    }

    private int what;
    private int ordinal = -1;
    private boolean couldCauseArithmeticConfusion;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(COMMAND_EVENT_CLASS) && !getParser().isCurrentEvent(ScriptCommandContext.class)) {
            Skript.error("The argument expression can only be used in command events");
            return false;
        }

        switch (matchedPattern) {
            case 0 -> what = LAST;
            case 1, 2 -> what = ORDINAL;
            case 3 -> {
                what = parseResult.mark == 1 ? ALL : SINGLE;
                couldCauseArithmeticConfusion = what == SINGLE && parseResult.expr.matches("(the )?arg(ument)?");
            }
            default -> throw new IllegalStateException("Unexpected pattern " + matchedPattern);
        }

        if (what == ORDINAL) {
            MatchResult regex = parseResult.regexes.get(0);
            String argMatch = null;
            for (int index = 1; index <= regex.groupCount(); index++) {
                String group = regex.group(index);
                if (group != null) {
                    argMatch = group;
                    break;
                }
            }
            if (argMatch == null) {
                return false;
            }
            ordinal = Integer.parseInt(argMatch);
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        // Script command context: return typed arguments as strings
        if (event.handle() instanceof ScriptCommandContext context) {
            return getFromScriptCommand(context);
        }

        String fullCommand = readCommand(event.handle());
        if (fullCommand == null || fullCommand.isBlank()) {
            return new String[0];
        }

        int firstSpace = fullCommand.indexOf(' ');
        if (firstSpace == -1 || firstSpace == fullCommand.length() - 1) {
            return new String[0];
        }

        String[] arguments = fullCommand.substring(firstSpace + 1).trim().split("\\s+");
        return switch (what) {
            case LAST -> arguments.length == 0 ? new String[0] : new String[]{arguments[arguments.length - 1]};
            case ORDINAL -> arguments.length < ordinal ? new String[0] : new String[]{arguments[ordinal - 1]};
            case SINGLE -> arguments.length == 1 ? new String[]{arguments[0]} : new String[0];
            case ALL -> arguments;
            default -> new String[0];
        };
    }

    private String[] getFromScriptCommand(ScriptCommandContext context) {
        Object[] parsedArgs = context.parsedArguments();
        if (parsedArgs == null || parsedArgs.length == 0) {
            return new String[0];
        }
        return switch (what) {
            case LAST -> {
                Object last = parsedArgs[parsedArgs.length - 1];
                yield last == null ? new String[0] : new String[]{String.valueOf(last)};
            }
            case ORDINAL -> {
                if (ordinal < 1 || ordinal > parsedArgs.length || parsedArgs[ordinal - 1] == null)
                    yield new String[0];
                yield new String[]{String.valueOf(parsedArgs[ordinal - 1])};
            }
            case SINGLE -> {
                if (parsedArgs.length == 1 && parsedArgs[0] != null)
                    yield new String[]{String.valueOf(parsedArgs[0])};
                yield new String[0];
            }
            case ALL -> {
                String[] result = new String[parsedArgs.length];
                for (int i = 0; i < parsedArgs.length; i++) {
                    result[i] = parsedArgs[i] == null ? "" : String.valueOf(parsedArgs[i]);
                }
                yield result;
            }
            default -> new String[0];
        };
    }

    @Override
    public boolean isSingle() {
        return what != ALL;
    }

    public boolean couldCauseArithmeticConfusion() {
        return couldCauseArithmeticConfusion;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (what) {
            case LAST -> "the last argument";
            case ORDINAL -> "argument " + ordinal;
            case ALL -> "the arguments";
            default -> "the argument";
        };
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
