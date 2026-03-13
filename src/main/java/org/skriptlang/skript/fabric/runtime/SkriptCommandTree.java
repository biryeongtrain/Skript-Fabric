package org.skriptlang.skript.fabric.runtime;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class SkriptCommandTree {

    private SkriptCommandTree() {
    }

    public interface SourceAccess<S> {
        boolean canUse(S source);
        void success(S source, String message);
        void failure(S source, String message);
    }

    public static <S> void register(
            CommandDispatcher<S> dispatcher,
            SkriptScriptService scriptService,
            SourceAccess<S> access
    ) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.<S>literal("skript")
                .requires(access::canUse)
                .then(LiteralArgumentBuilder.<S>literal("reload")
                        .then(LiteralArgumentBuilder.<S>literal("all")
                                .executes(context -> execute(context, scriptService, access, "reload all", scriptService::reloadAll)))
                        .then(LiteralArgumentBuilder.<S>literal("scripts")
                                .executes(context -> execute(context, scriptService, access, "reload scripts", scriptService::reloadScripts)))
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<S, String>argument("target", StringArgumentType.greedyString())
                                .suggests((context, builder) -> suggest(scriptService, builder))
                                .executes(context -> executeTarget(
                                        context,
                                        access,
                                        "reload",
                                        StringArgumentType.getString(context, "target"),
                                        scriptService::reloadTarget
                                ))))
                .then(LiteralArgumentBuilder.<S>literal("enable")
                        .then(LiteralArgumentBuilder.<S>literal("all")
                                .executes(context -> execute(context, scriptService, access, "enable all", scriptService::enableAll)))
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<S, String>argument("target", StringArgumentType.greedyString())
                                .suggests((context, builder) -> suggest(scriptService, builder))
                                .executes(context -> executeTarget(
                                        context,
                                        access,
                                        "enable",
                                        StringArgumentType.getString(context, "target"),
                                        scriptService::enableTarget
                                ))))
                .then(LiteralArgumentBuilder.<S>literal("disable")
                        .then(LiteralArgumentBuilder.<S>literal("all")
                                .executes(context -> execute(context, scriptService, access, "disable all", scriptService::disableAll)))
                        .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<S, String>argument("target", StringArgumentType.greedyString())
                                .suggests((context, builder) -> suggest(scriptService, builder))
                                .executes(context -> executeTarget(
                                        context,
                                        access,
                                        "disable",
                                        StringArgumentType.getString(context, "target"),
                                        scriptService::disableTarget
                                ))))
                .then(LiteralArgumentBuilder.<S>literal("list")
                        .executes(context -> list(context, scriptService, access)))
                .then(LiteralArgumentBuilder.<S>literal("help")
                        .executes(context -> help(context, scriptService, access)))
                .executes(context -> help(context, scriptService, access));

        dispatcher.register(root);
    }

    private static <S> int execute(
            CommandContext<S> context,
            SkriptScriptService service,
            SourceAccess<S> access,
            String label,
            Operation supplier
    ) {
        try {
            SkriptScriptOperationResult result = supplier.run();
            access.success(context.getSource(), summarize(label, result));
            return result.affectedFiles();
        } catch (IOException exception) {
            access.failure(context.getSource(), failure(label, exception));
            return 0;
        }
    }

    private static <S> int executeTarget(
            CommandContext<S> context,
            SourceAccess<S> access,
            String verb,
            String target,
            TargetOperation supplier
    ) {
        String label = verb + " " + target;
        try {
            SkriptScriptOperationResult result = supplier.run(target);
            access.success(context.getSource(), summarize(label, result));
            return result.affectedFiles();
        } catch (IOException exception) {
            access.failure(context.getSource(), failure(label, exception));
            return 0;
        }
    }

    private static <S> int list(CommandContext<S> context, SkriptScriptService service, SourceAccess<S> access) {
        List<String> scripts = service.listLoadedScripts();
        if (scripts.isEmpty()) {
            access.success(context.getSource(), "Loaded scripts: none");
            return 0;
        }
        access.success(context.getSource(), "Loaded scripts (" + scripts.size() + "): " + String.join(", ", scripts));
        return scripts.size();
    }

    private static <S> int help(CommandContext<S> context, SkriptScriptService service, SourceAccess<S> access) {
        access.success(
                context.getSource(),
                "Usage: /skript reload all|scripts|<target>, /skript enable all|<target>, /skript disable all|<target>, /skript list, /skript help"
        );
        return 1;
    }

    private static CompletableFuture<Suggestions> suggest(SkriptScriptService service, SuggestionsBuilder builder) throws CommandSyntaxException {
        String remaining = builder.getRemainingLowerCase();
        for (String suggestion : service.suggestedTargets()) {
            if (remaining.isBlank() || suggestion.toLowerCase(Locale.ENGLISH).startsWith(remaining)) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }

    private static String summarize(String label, SkriptScriptOperationResult result) {
        if (result.affectedFiles() == 0) {
            return capitalize(label) + ": no matching scripts";
        }
        return capitalize(label) + ": " + result.affectedFiles() + " script(s) affected"
                + (result.scripts().isEmpty() ? "" : " [" + String.join(", ", result.scripts()) + "]");
    }

    private static String failure(String label, IOException exception) {
        String message = exception.getMessage();
        return capitalize(label) + " failed" + (message == null || message.isBlank() ? "." : ": " + message);
    }

    private static String capitalize(String input) {
        if (input.isEmpty()) {
            return input;
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    @FunctionalInterface
    private interface Operation {
        SkriptScriptOperationResult run() throws IOException;
    }

    @FunctionalInterface
    private interface TargetOperation {
        SkriptScriptOperationResult run(String target) throws IOException;
    }
}
