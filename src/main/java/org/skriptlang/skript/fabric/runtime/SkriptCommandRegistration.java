package org.skriptlang.skript.fabric.runtime;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class SkriptCommandRegistration {

    private SkriptCommandRegistration() {
    }

    public static void register(SkriptScriptService scriptService) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SkriptCommandTree.register(dispatcher, scriptService, SkriptCommandSourceAccess.INSTANCE));
    }
}
