package org.skriptlang.skript.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.skriptlang.skript.fabric.api.SkriptAddonEntrypoint;
import org.skriptlang.skript.fabric.runtime.SkriptCommandRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntimeLifecycle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntimeServices;

public final class SkriptFabric implements ModInitializer {

    public static final String MOD_ID = "skfabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SkriptFabricBootstrap.bootstrap();
        invokeAddonEntrypoints();
        SkriptRuntimeLifecycle.register(SkriptRuntimeServices.scriptService(), LOGGER);
        SkriptCommandRegistration.register(SkriptRuntimeServices.scriptService());
    }

    private void invokeAddonEntrypoints() {
        var entrypoints = FabricLoader.getInstance()
                .getEntrypoints("skript", SkriptAddonEntrypoint.class);
        for (var addon : entrypoints) {
            try {
                LOGGER.info("Initializing Skript addon: {}", addon.getClass().getName());
                addon.onSkriptInitialize();
            } catch (Exception exception) {
                LOGGER.error("Failed to initialize Skript addon: {}",
                        addon.getClass().getName(), exception);
            }
        }
        if (!entrypoints.isEmpty()) {
            LOGGER.info("Initialized {} Skript addon(s).", entrypoints.size());
        }
    }
}
