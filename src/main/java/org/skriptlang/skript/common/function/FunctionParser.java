package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.Nullable;

public class FunctionParser {

    public static @Nullable ch.njol.skript.lang.function.Signature<?> parse(
            String script,
            String name,
            String args,
            @Nullable String returns,
            boolean local
    ) {
        return ch.njol.skript.lang.function.FunctionParser.parse(script, name, args, returns, local);
    }
}
