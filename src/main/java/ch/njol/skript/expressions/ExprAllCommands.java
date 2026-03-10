package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.mojang.brigadier.tree.CommandNode;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@SuppressWarnings("unused")
public class ExprAllCommands extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprAllCommands.class, String.class,
                "[(all|the|all [of] the)] [registered] [(1¦script)] commands");
    }

    private boolean scriptCommandsOnly;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        scriptCommandsOnly = parseResult.mark == 1;
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (event.server() == null || scriptCommandsOnly) {
            return new String[0];
        }
        return event.server().getCommands().getDispatcher().getRoot().getChildren().stream()
                .map(CommandNode::getName)
                .filter(name -> !name.isBlank())
                .toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "all " + (scriptCommandsOnly ? "script " : "") + "commands";
    }
}
