package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.ScriptLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.skriptlang.skript.lang.script.Script;

final class SkriptScriptManager {

    private final Path root;
    private final SkriptRuntime runtime;

    SkriptScriptManager(Path root, SkriptRuntime runtime) {
        this.root = root.toAbsolutePath().normalize();
        this.runtime = runtime;
    }

    Path root() {
        return root;
    }

    List<String> discoverScripts() throws IOException {
        ensureRootDirectory();
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(this::isScriptFile)
                    .map(root::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }
    }

    List<String> loadedScriptNames() {
        return runtime.loadedScripts().stream()
                .map(this::loadedScriptName)
                .filter(name -> name != null && !name.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    List<String> suggestedTargets() throws IOException {
        ensureRootDirectory();
        Set<String> targets = new LinkedHashSet<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> !path.equals(root))
                    .sorted()
                    .forEach(path -> {
                        if (Files.isDirectory(path)) {
                            targets.add(root.relativize(path).toString().replace('\\', '/'));
                        } else if (isScriptFile(path)) {
                            targets.add(targetName(path));
                        }
                    });
        }
        return List.copyOf(targets);
    }

    ScriptLoadResult loadAll() throws IOException {
        ensureRootDirectory();
        runtime.clearScripts();
        return loadRelativePaths(discoverEnabledScripts(root));
    }

    ScriptToggleResult unloadAll() {
        List<String> loaded = loadedScriptNames();
        runtime.clearScripts();
        return new ScriptToggleResult(loaded, Map.of());
    }

    ScriptLoadResult reloadAll() throws IOException {
        return loadAll();
    }

    ScriptLoadResult reloadScripts() throws IOException {
        return loadAll();
    }

    ScriptLoadResult reloadTarget(String target) throws IOException {
        ensureRootDirectory();
        ResolvedTarget resolved = resolveTarget(target);
        if (resolved == null) {
            return new ScriptLoadResult(List.of(), Map.of());
        }
        unloadMatching(resolved.scopePath(), resolved.directoryTarget());
        return loadRelativePaths(resolved.enabledPaths());
    }

    ScriptToggleResult enableAll() throws IOException {
        ensureRootDirectory();
        List<Path> renamed = new ArrayList<>();
        for (Path relativePath : discoverDisabledScripts(root)) {
            Path source = root.resolve(relativePath);
            Path enabled = toEnabledPath(source);
            Files.move(source, enabled, StandardCopyOption.REPLACE_EXISTING);
            renamed.add(root.relativize(enabled));
        }
        loadRelativePaths(renamed);
        return new ScriptToggleResult(userFacingNames(renamed), Map.of());
    }

    ScriptToggleResult enableTarget(String target) throws IOException {
        ensureRootDirectory();
        ResolvedTarget resolved = resolveTarget(target);
        if (resolved == null) {
            return new ScriptToggleResult(List.of(), Map.of());
        }
        List<Path> renamed = new ArrayList<>();
        for (Path relativePath : resolved.allPaths()) {
            Path source = root.resolve(relativePath);
            if (!isDisabledScriptFile(source)) {
                continue;
            }
            Path enabled = toEnabledPath(source);
            Files.move(source, enabled, StandardCopyOption.REPLACE_EXISTING);
            renamed.add(root.relativize(enabled));
        }
        ScriptLoadResult loadResult = loadRelativePaths(renamed);
        return new ScriptToggleResult(loadResult.scripts(), loadResult.errors());
    }

    ScriptToggleResult disableAll() throws IOException {
        ensureRootDirectory();
        List<Path> enabledPaths = discoverEnabledScripts(root);
        runtime.clearScripts();
        List<Path> renamed = new ArrayList<>();
        for (Path relativePath : enabledPaths) {
            Path source = root.resolve(relativePath);
            Path disabled = toDisabledPath(source);
            Files.move(source, disabled, StandardCopyOption.REPLACE_EXISTING);
            renamed.add(root.relativize(disabled));
        }
        return new ScriptToggleResult(userFacingNames(renamed), Map.of());
    }

    ScriptToggleResult disableTarget(String target) throws IOException {
        ensureRootDirectory();
        ResolvedTarget resolved = resolveTarget(target);
        if (resolved == null) {
            return new ScriptToggleResult(List.of(), Map.of());
        }
        unloadMatching(resolved.scopePath(), resolved.directoryTarget());
        List<Path> renamed = new ArrayList<>();
        for (Path relativePath : resolved.allPaths()) {
            Path source = root.resolve(relativePath);
            if (!isEnabledScriptFile(source)) {
                continue;
            }
            Path disabled = toDisabledPath(source);
            Files.move(source, disabled, StandardCopyOption.REPLACE_EXISTING);
            renamed.add(root.relativize(disabled));
        }
        return new ScriptToggleResult(userFacingNames(renamed), Map.of());
    }

    private void ensureRootDirectory() throws IOException {
        Files.createDirectories(root);
    }

    private ScriptLoadResult loadRelativePaths(Collection<Path> relativePaths) throws IOException {
        List<Path> loaded = new ArrayList<>();
        Map<String, String> errors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Path relativePath : sortedPaths(relativePaths)) {
            Path absolutePath = root.resolve(relativePath);
            if (!Files.isRegularFile(absolutePath) || !isEnabledScriptFile(absolutePath)) {
                continue;
            }
            try {
                runtime.loadFromPath(absolutePath, relativePath.toString().replace('\\', '/'));
                loaded.add(relativePath);
            } catch (IOException exception) {
                errors.put(targetName(absolutePath), describeError(exception));
            } catch (RuntimeException exception) {
                errors.put(targetName(absolutePath), describeError(exception));
            }
        }
        return new ScriptLoadResult(userFacingNames(loaded), errors);
    }

    private String describeError(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }

    private void unloadMatching(Path scopePath, boolean directoryTarget) {
        List<Script> scripts = runtime.loadedScripts().stream()
                .filter(script -> scriptFile(script)
                        .map(path -> matchesScope(path, scopePath, directoryTarget))
                        .orElse(false))
                .toList();
        runtime.unloadScripts(scripts);
    }

    private boolean matchesScope(Path scriptPath, Path scopePath, boolean directoryTarget) {
        Path normalized = scriptPath.toAbsolutePath().normalize();
        return directoryTarget ? normalized.startsWith(scopePath) : normalized.equals(scopePath);
    }

    private String loadedScriptName(Script script) {
        Optional<Path> path = scriptFile(script);
        if (path.isPresent() && path.get().startsWith(root)) {
            return targetName(path.get());
        }
        return script.getConfig() == null ? null : script.getConfig().getFileName();
    }

    private Optional<Path> scriptFile(Script script) {
        if (script.getConfig() == null || script.getConfig().getFile() == null) {
            return Optional.empty();
        }
        return Optional.of(script.getConfig().getFile().toPath().toAbsolutePath().normalize());
    }

    private ResolvedTarget resolveTarget(String rawTarget) throws IOException {
        String target = rawTarget == null ? "" : rawTarget.trim();
        if (target.isEmpty()) {
            return null;
        }
        Path relativeTarget = normalizeRelativeTarget(target);
        Path resolved = root.resolve(relativeTarget).normalize();
        if (Files.isDirectory(resolved)) {
            return new ResolvedTarget(resolved, true, collectScriptFiles(resolved), discoverEnabledScripts(resolved));
        }
        Path file = resolveScriptFile(resolved);
        if (file == null) {
            return null;
        }
        Path relativeFile = root.relativize(file);
        List<Path> enabled = isEnabledScriptFile(file) ? List.of(relativeFile) : List.of();
        return new ResolvedTarget(file, false, List.of(relativeFile), enabled);
    }

    private List<Path> collectScriptFiles(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(this::isScriptFile)
                    .map(root::relativize)
                    .sorted()
                    .toList();
        }
    }

    private Path resolveScriptFile(Path resolved) {
        if (Files.isRegularFile(resolved) && isScriptFile(resolved)) {
            return resolved;
        }
        String fileName = resolved.getFileName() == null ? "" : resolved.getFileName().toString();
        if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".sk")) {
            Path enabled = resolved.resolveSibling(fileName + ".sk");
            if (Files.isRegularFile(enabled) && isScriptFile(enabled)) {
                return enabled;
            }
            Path disabled = resolved.resolveSibling(ScriptLoader.DISABLED_SCRIPT_PREFIX + fileName + ".sk");
            if (Files.isRegularFile(disabled) && isScriptFile(disabled)) {
                return disabled;
            }
        }
        return null;
    }

    private Path normalizeRelativeTarget(String target) throws IOException {
        try {
            Path relative = Path.of(target.replace('\\', '/')).normalize();
            if (relative.isAbsolute() || relative.startsWith("..")) {
                throw new IOException("Script target must stay within config/skript");
            }
            return relative;
        } catch (RuntimeException exception) {
            throw new IOException("Invalid script target: " + target, exception);
        }
    }

    private List<Path> discoverEnabledScripts(Path directory) throws IOException {
        return discoverScripts(directory, true);
    }

    private List<Path> discoverDisabledScripts(Path directory) throws IOException {
        return discoverScripts(directory, false);
    }

    private List<Path> discoverScripts(Path directory, boolean enabled) throws IOException {
        if (!Files.exists(directory)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> enabled ? isEnabledScriptFile(path) : isDisabledScriptFile(path))
                    .map(root::relativize)
                    .sorted()
                    .toList();
        }
    }

    private boolean isScriptFile(Path path) {
        String fileName = path.getFileName() == null ? "" : path.getFileName().toString();
        return fileName.toLowerCase(Locale.ENGLISH).endsWith(".sk");
    }

    private boolean isEnabledScriptFile(Path path) {
        String fileName = path.getFileName() == null ? "" : path.getFileName().toString();
        return isScriptFile(path) && !fileName.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX);
    }

    private boolean isDisabledScriptFile(Path path) {
        String fileName = path.getFileName() == null ? "" : path.getFileName().toString();
        return isScriptFile(path) && fileName.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX);
    }

    private Path toEnabledPath(Path path) {
        String fileName = path.getFileName().toString();
        if (!fileName.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX)) {
            return path;
        }
        return path.resolveSibling(fileName.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH));
    }

    private Path toDisabledPath(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX)) {
            return path;
        }
        return path.resolveSibling(ScriptLoader.DISABLED_SCRIPT_PREFIX + fileName);
    }

    private String targetName(Path absolutePath) {
        Path relative = root.relativize(absolutePath.toAbsolutePath().normalize());
        String name = relative.toString().replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        String prefix = lastSlash >= 0 ? name.substring(0, lastSlash + 1) : "";
        String fileName = lastSlash >= 0 ? name.substring(lastSlash + 1) : name;
        if (fileName.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX)) {
            fileName = fileName.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH);
        }
        if (fileName.toLowerCase(Locale.ENGLISH).endsWith(".sk")) {
            fileName = fileName.substring(0, fileName.length() - 3);
        }
        return prefix + fileName;
    }

    private List<String> userFacingNames(Collection<Path> relativePaths) {
        return sortedPaths(relativePaths).stream()
                .map(relative -> targetName(root.resolve(relative)))
                .toList();
    }

    private List<Path> sortedPaths(Collection<Path> paths) {
        return paths.stream()
                .sorted(Comparator.comparing(path -> path.toString().replace('\\', '/'), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private record ResolvedTarget(Path scopePath, boolean directoryTarget, List<Path> allPaths, List<Path> enabledPaths) {
    }

    record ScriptLoadResult(List<String> scripts, Map<String, String> errors) {
    }

    record ScriptToggleResult(List<String> scripts, Map<String, String> errors) {
    }
}
