package org.skriptlang.skript.fabric.runtime;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

final class UnsupportedSkriptScriptService implements SkriptScriptService {

    private static final String MESSAGE = "Skript reload service is not implemented in this build.";

    private final Path root;

    UnsupportedSkriptScriptService(Path root) {
        this.root = root;
    }

    @Override
    public Path root() {
        return root;
    }

    @Override
    public SkriptScriptOperationResult loadAll() throws IOException {
        throw unsupported();
    }

    @Override
    public SkriptScriptOperationResult reloadAll() throws IOException {
        throw unsupported();
    }

    @Override
    public SkriptScriptOperationResult reloadScripts() throws IOException {
        throw unsupported();
    }

    @Override
    public SkriptScriptOperationResult reloadTarget(String target) throws IOException {
        throw unsupported();
    }

    @Override
    public SkriptScriptOperationResult enableAll() throws IOException {
        throw unsupported();
    }

    @Override
    public SkriptScriptOperationResult enableTarget(String target) throws IOException {
        throw unsupported();
    }

    @Override
    public SkriptScriptOperationResult disableAll() throws IOException {
        throw unsupported();
    }

    @Override
    public SkriptScriptOperationResult disableTarget(String target) throws IOException {
        throw unsupported();
    }

    @Override
    public List<String> listLoadedScripts() {
        return List.of();
    }

    @Override
    public List<String> suggestedTargets() {
        return List.of();
    }

    @Override
    public void shutdown() {
    }

    private IOException unsupported() {
        return new IOException(MESSAGE);
    }
}
