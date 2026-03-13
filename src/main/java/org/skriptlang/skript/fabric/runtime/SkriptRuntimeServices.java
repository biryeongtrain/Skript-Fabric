package org.skriptlang.skript.fabric.runtime;

public final class SkriptRuntimeServices {

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
                scriptService = new FileSystemSkriptScriptService(SkriptScriptRoots.runtimeScriptsRoot(), SkriptRuntime.instance());
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
