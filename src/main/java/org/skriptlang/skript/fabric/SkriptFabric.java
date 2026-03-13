package org.skriptlang.skript.fabric;

import net.fabricmc.api.ModInitializer;
import org.skriptlang.skript.fabric.runtime.SkriptCommandRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntimeLifecycle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntimeServices;

public final class SkriptFabric implements ModInitializer {

    public static final String MOD_ID = "skript-fabric-port";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SkriptFabricBootstrap.bootstrap();
        SkriptRuntimeLifecycle.register(SkriptRuntimeServices.scriptService(), LOGGER);
        SkriptCommandRegistration.register(SkriptRuntimeServices.scriptService());
    }
}
