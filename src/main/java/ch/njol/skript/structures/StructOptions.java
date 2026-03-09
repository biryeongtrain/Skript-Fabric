package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public final class StructOptions extends Structure {

    public static final Priority PRIORITY = new Priority(100);
    private static final String OPTION_ENTRY_KEY = "__option_entry__";
    private static final String OPTION_SECTION_KEY = "__option_section__";
    private static final EntryValidator ENTRY_VALIDATOR = EntryValidator.builder()
            .addEntryData(new OptionEntryData())
            .addEntryData(new OptionSectionData())
            .unexpectedNodeTester(node -> false)
            .build();

    private @Nullable SectionNode source;

    private StructOptions() {
    }

    public static void register() {
        Skript.registerStructure(StructOptions.class, SyntaxInfo.Structure.NodeType.SECTION, ENTRY_VALIDATOR, "options");
    }

    @Override
    public boolean init(
            Literal<?>[] args,
            int matchedPattern,
            ParseResult parseResult,
            @Nullable EntryContainer entryContainer
    ) {
        source = getParser().getData(StructureData.class).node instanceof SectionNode sectionNode ? sectionNode : null;
        if (source == null) {
            return false;
        }
        ScriptLoader.OptionsData optionsData = getParser().getCurrentScript()
                .getData(ScriptLoader.OptionsData.class, ScriptLoader.OptionsData::new);
        return loadOptions(source, "", optionsData, entryContainer);
    }

    private boolean loadOptions(
            SectionNode sectionNode,
            String prefix,
            ScriptLoader.OptionsData optionsData,
            @Nullable EntryContainer entryContainer
    ) {
        EntryContainer container = entryContainer != null ? entryContainer : ENTRY_VALIDATOR.validate(sectionNode);
        if (container == null) {
            return false;
        }

        for (Node node : container.getUnhandledNodes()) {
            if (node instanceof SimpleNode) {
                Skript.error("Invalid line in options");
            }
        }

        for (EntryNode entryNode : container.getAll(OPTION_ENTRY_KEY, EntryNode.class, false)) {
            String key = entryNode.getKey();
            String value = entryNode.getValue();
            if (key == null || key.isBlank() || value == null) {
                continue;
            }
            optionsData.put(prefix + key, value);
        }

        for (SectionNode childSection : container.getAll(OPTION_SECTION_KEY, SectionNode.class, false)) {
            String childKey = childSection.getKey();
            if (childKey == null || childKey.isBlank()) {
                continue;
            }
            if (!loadOptions(childSection, prefix + childKey + ".", optionsData, null)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public void unload() {
        if (getParser().getCurrentScript() != null) {
            getParser().getCurrentScript().removeData(ScriptLoader.OptionsData.class);
        }
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "options";
    }

    private static final class OptionEntryData extends EntryData<EntryNode> {

        private OptionEntryData() {
            super(OPTION_ENTRY_KEY, null, true, true);
        }

        @Override
        public @Nullable EntryNode getValue(Node node) {
            if (node instanceof EntryNode entryNode) {
                return entryNode;
            }
            if (!(node instanceof SimpleNode simpleNode)) {
                return null;
            }
            String key = simpleNode.getKey();
            if (key == null) {
                return null;
            }
            int separator = key.indexOf(':');
            if (separator < 0) {
                return null;
            }
            EntryNode entryNode = new EntryNode(
                    key.substring(0, separator).trim(),
                    key.substring(separator + 1).trim()
            );
            entryNode.setLine(simpleNode.getLine());
            entryNode.setDebug(simpleNode.debug());
            return entryNode;
        }

        @Override
        public boolean canCreateWith(Node node) {
            if (node instanceof EntryNode entryNode) {
                String key = entryNode.getKey();
                return key != null && !key.isBlank();
            }
            if (!(node instanceof SimpleNode simpleNode)) {
                return false;
            }
            String key = simpleNode.getKey();
            if (key == null) {
                return false;
            }
            int separator = key.indexOf(':');
            return separator > 0;
        }
    }

    private static final class OptionSectionData extends EntryData<SectionNode> {

        private OptionSectionData() {
            super(OPTION_SECTION_KEY, null, true, true);
        }

        @Override
        public @Nullable SectionNode getValue(Node node) {
            return node instanceof SectionNode sectionNode ? sectionNode : null;
        }

        @Override
        public boolean canCreateWith(Node node) {
            if (!(node instanceof SectionNode sectionNode)) {
                return false;
            }
            String key = sectionNode.getKey();
            return key != null && !key.isBlank();
        }
    }
}
