package org.skriptlang.skript.fabric.runtime;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface SkriptScriptService {

    Path root();

    List<String> discoverScripts() throws IOException;

    SkriptScriptOperationResult loadAll() throws IOException;

    SkriptScriptOperationResult unloadAll();

    SkriptScriptOperationResult reloadAll() throws IOException;

    SkriptScriptOperationResult reloadScripts() throws IOException;

    SkriptScriptOperationResult reloadTarget(String target) throws IOException;

    SkriptScriptOperationResult enableAll() throws IOException;

    SkriptScriptOperationResult enableTarget(String target) throws IOException;

    SkriptScriptOperationResult disableAll() throws IOException;

    SkriptScriptOperationResult disableTarget(String target) throws IOException;

    List<String> listLoadedScripts();

    List<String> suggestedTargets();

    void shutdown();
}
