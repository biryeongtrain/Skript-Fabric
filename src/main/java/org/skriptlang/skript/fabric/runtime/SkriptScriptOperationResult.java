package org.skriptlang.skript.fabric.runtime;

import java.util.List;

public record SkriptScriptOperationResult(int affectedFiles, List<String> scripts) {

    public SkriptScriptOperationResult {
        scripts = List.copyOf(scripts);
    }
}
