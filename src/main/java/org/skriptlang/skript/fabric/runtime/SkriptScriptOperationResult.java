package org.skriptlang.skript.fabric.runtime;

import java.util.List;
import java.util.Map;

public record SkriptScriptOperationResult(int affectedFiles, List<String> scripts, Map<String, String> errors) {

    public SkriptScriptOperationResult {
        scripts = List.copyOf(scripts);
        errors = Map.copyOf(errors);
    }

    public SkriptScriptOperationResult(int affectedFiles, List<String> scripts) {
        this(affectedFiles, scripts, Map.of());
    }
}
