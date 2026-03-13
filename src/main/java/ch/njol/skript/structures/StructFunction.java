package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionParser;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionReference;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public class StructFunction extends Structure {

    public static final Priority PRIORITY = new Priority(400);

    private static final Pattern SIGNATURE_PATTERN = Pattern.compile(
            "^(?:local )?function (?<name>" + Functions.functionNamePattern + ")\\((?<args>.*?)\\)"
                    + "(?:\\s*(?:->|::| returns )\\s*(?<returns>.+))?$"
    );
    private static final AtomicBoolean VALIDATE_FUNCTIONS = new AtomicBoolean();

    private @Nullable SectionNode source;
    private @Nullable Signature<?> signature;
    private boolean local;

    public static void register() {
        Skript.registerStructure(
                StructFunction.class,
                SyntaxInfo.Structure.NodeType.SECTION,
                "[:local] function <.+>"
        );
    }

    @Override
    public boolean init(
            Literal<?>[] literals,
            int matchedPattern,
            ParseResult parseResult,
            @Nullable EntryContainer entryContainer
    ) {
        if (entryContainer == null) {
            return false;
        }
        source = entryContainer.getSource();
        local = parseResult.hasTag("local");
        return source != null;
    }

    @Override
    public boolean preLoad() {
        if (source == null) {
            return false;
        }

        String rawSignature = source.getKey();
        if (rawSignature == null) {
            return false;
        }
        rawSignature = ScriptLoader.replaceOptions(rawSignature);

        Matcher matcher = SIGNATURE_PATTERN.matcher(rawSignature);
        if (!matcher.matches()) {
            Skript.error("Invalid function signature: " + rawSignature);
            return false;
        }

        getParser().setCurrentEvent((local ? "local " : "") + "function", FunctionEvent.class);
        try {
            if (getParser().getCurrentScript() == null || getParser().getCurrentScript().getConfig() == null) {
                Skript.error("Cannot load function without an active script.");
                return false;
            }
            signature = FunctionParser.parse(
                    getParser().getCurrentScript().getConfig().getFileName(),
                    matcher.group("name"),
                    matcher.group("args"),
                    matcher.group("returns"),
                    local
            );
        } finally {
            getParser().deleteCurrentEvent();
        }

        return signature != null && Functions.registerSignature(signature) != null;
    }

    @Override
    public boolean load() {
        if (signature == null || source == null || getParser().getCurrentScript() == null) {
            return false;
        }

        getParser().setCurrentEvent((local ? "local " : "") + "function", FunctionEvent.class);
        try {
            if (Functions.loadFunction(getParser().getCurrentScript(), source, signature) == null) {
                return false;
            }
        } finally {
            getParser().deleteCurrentEvent();
        }

        VALIDATE_FUNCTIONS.set(true);
        return true;
    }

    @Override
    public boolean postLoad() {
        if (VALIDATE_FUNCTIONS.compareAndSet(true, false)) {
            Functions.validateFunctions();
        }
        return true;
    }

    @Override
    public void unload() {
        if (signature == null) {
            return;
        }
        Functions.unregisterFunction(signature);
        for (Object call : signature.calls()) {
            if (call instanceof FunctionReference<?> reference) {
                reference.invalidateCache();
            }
        }
        VALIDATE_FUNCTIONS.set(true);
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return (local ? "local " : "") + "function";
    }
}
