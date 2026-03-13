package org.skriptlang.skript.fabric.runtime;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class SkriptScriptRoots {

    private SkriptScriptRoots() {
    }

    public static Path runtimeScriptsRoot() {
        return FabricLoader.getInstance().getConfigDir().resolve("skript");
    }
}
