package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

/**
 * Registers the {@code aliases:} structure pattern so that scripts using aliases
 * do not fail to parse. Item aliases are not meaningful in the Fabric runtime
 * because items are referenced by vanilla registry IDs.
 */
public final class StructAliases extends Structure {

    public static final Priority PRIORITY = new Priority(200);

    public static void register() {
        Skript.registerStructure(StructAliases.class, SyntaxInfo.Structure.NodeType.SECTION, "aliases");
    }

    @Override
    public boolean init(
            Literal<?>[] args,
            int matchedPattern,
            ParseResult parseResult,
            @Nullable EntryContainer entryContainer
    ) {
        Skript.warning("Item aliases are not supported in the Fabric runtime. Use vanilla item IDs instead.");
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "aliases";
    }
}
