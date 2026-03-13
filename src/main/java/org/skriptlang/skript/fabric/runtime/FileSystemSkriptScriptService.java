package org.skriptlang.skript.fabric.runtime;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

final class FileSystemSkriptScriptService implements SkriptScriptService {

    private final SkriptScriptManager manager;

    FileSystemSkriptScriptService(SkriptScriptManager manager) {
        this.manager = manager;
    }

    FileSystemSkriptScriptService(Path root, SkriptRuntime runtime) {
        this(new SkriptScriptManager(root, runtime));
    }

    @Override
    public Path root() {
        return manager.root();
    }

    @Override
    public List<String> discoverScripts() throws IOException {
        return manager.discoverScripts();
    }

    @Override
    public SkriptScriptOperationResult loadAll() throws IOException {
        return result(manager.loadAll());
    }

    @Override
    public SkriptScriptOperationResult unloadAll() {
        return result(manager.unloadAll());
    }

    @Override
    public SkriptScriptOperationResult reloadAll() throws IOException {
        return result(manager.reloadAll());
    }

    @Override
    public SkriptScriptOperationResult reloadScripts() throws IOException {
        return result(manager.reloadScripts());
    }

    @Override
    public SkriptScriptOperationResult reloadTarget(String target) throws IOException {
        return result(manager.reloadTarget(target));
    }

    @Override
    public SkriptScriptOperationResult enableAll() throws IOException {
        return result(manager.enableAll());
    }

    @Override
    public SkriptScriptOperationResult enableTarget(String target) throws IOException {
        return result(manager.enableTarget(target));
    }

    @Override
    public SkriptScriptOperationResult disableAll() throws IOException {
        return result(manager.disableAll());
    }

    @Override
    public SkriptScriptOperationResult disableTarget(String target) throws IOException {
        return result(manager.disableTarget(target));
    }

    @Override
    public List<String> listLoadedScripts() {
        return manager.loadedScriptNames();
    }

    @Override
    public List<String> suggestedTargets() {
        try {
            return manager.suggestedTargets();
        } catch (IOException exception) {
            return List.of();
        }
    }

    @Override
    public void shutdown() {
        manager.unloadAll();
    }

    private SkriptScriptOperationResult result(SkriptScriptManager.ScriptLoadResult result) {
        return new SkriptScriptOperationResult(result.scripts().size(), result.scripts(), result.errors());
    }

    private SkriptScriptOperationResult result(SkriptScriptManager.ScriptToggleResult result) {
        return new SkriptScriptOperationResult(result.scripts().size(), result.scripts(), result.errors());
    }
}
