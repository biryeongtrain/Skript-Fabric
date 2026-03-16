package org.skriptlang.skript.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.Component;
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
    public static MinecraftAudiences ADVENTURE= null;
    @Override
    public void onInitialize() {
        SkriptFabricBootstrap.bootstrap();
        invokeAddonEntrypoints();
        SkriptRuntimeLifecycle.register(SkriptRuntimeServices.scriptService(), LOGGER);
        SkriptCommandRegistration.register(SkriptRuntimeServices.scriptService());
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ADVENTURE = MinecraftServerAudiences.of(server));
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

    public static Component byMiniMessage(String value) {
        return ADVENTURE.asNative(MiniMessage.miniMessage().deserialize(value));
    }
}
