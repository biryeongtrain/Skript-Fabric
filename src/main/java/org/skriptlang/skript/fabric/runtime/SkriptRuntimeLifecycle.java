package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.command.Commands;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;

public final class SkriptRuntimeLifecycle {

    private SkriptRuntimeLifecycle() {
    }

    public static void register(SkriptScriptService scriptService, Logger logger) {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Commands.setServer(server);
            onServerStarted(scriptService, logger);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            onServerStopping(scriptService, logger);
            Commands.setServer(null);
        });
    }

    static void onServerStarted(SkriptScriptService scriptService, Logger logger) {
        try {
            SkriptScriptOperationResult result = scriptService.loadAll();
            logger.info("Loaded {} Skript script(s) from {}", result.affectedFiles(), scriptService.root());
        } catch (Exception exception) {
            logger.error("Failed to load Skript scripts from {}", scriptService.root(), exception);
        }
    }

    static void onServerStopping(SkriptScriptService scriptService, Logger logger) {
        try {
            scriptService.shutdown();
        } catch (Exception exception) {
            logger.error("Failed to shut down Skript scripts from {}", scriptService.root(), exception);
        }
    }
}
