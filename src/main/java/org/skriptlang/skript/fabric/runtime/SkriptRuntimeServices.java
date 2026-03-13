package org.skriptlang.skript.fabric.runtime;

import java.nio.file.Path;

public final class SkriptRuntimeServices {

    private static final Path DEFAULT_ROOT = Path.of("config", "skript");
    private static volatile SkriptScriptService scriptService;

    private SkriptRuntimeServices() {
    }

    public static SkriptScriptService scriptService() {
        SkriptScriptService current = scriptService;
        if (current != null) {
            return current;
        }
        synchronized (SkriptRuntimeServices.class) {
            if (scriptService == null) {
                scriptService = new UnsupportedSkriptScriptService(DEFAULT_ROOT);
            }
            return scriptService;
        }
    }

    static synchronized void setScriptServiceForTesting(SkriptScriptService service) {
        scriptService = service;
    }

    static synchronized void resetScriptServiceForTesting() {
        scriptService = null;
    }
}
