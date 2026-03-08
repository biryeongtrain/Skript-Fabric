package ch.njol.skript.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.SyntaxInfo;

class StructureEntryValidatorCompatibilityTest {

    private static final EntryValidator VALIDATOR = EntryValidator.builder()
            .addEntry("label", "fallback_block", true)
            .addSection("trigger", false)
            .build();

    @BeforeEach
    void resetState() {
        Skript.instance().syntaxRegistry().clearAll();
        ValidatorStructure.lastLabel = null;
        ValidatorStructure.lastTrigger = null;
    }

    @AfterEach
    void cleanup() {
        Skript.instance().syntaxRegistry().clearAll();
    }

    @Test
    void structureParsePassesEntryNodeValuesThroughEntryValidator() {
        Skript.registerStructure(
                ValidatorStructure.class,
                SyntaxInfo.Structure.NodeType.SECTION,
                VALIDATOR,
                "validated structure"
        );

        SectionNode node = new SectionNode("validated structure");
        EntryNode label = new EntryNode("LABEL", "emerald_block");
        SectionNode trigger = new SectionNode("TRIGGER");
        node.add(label);
        node.add(trigger);

        org.skriptlang.skript.lang.structure.Structure parsed = org.skriptlang.skript.lang.structure.Structure.parse(
                "validated structure",
                node,
                "failed"
        );

        assertNotNull(parsed);
        assertInstanceOf(ValidatorStructure.class, parsed);
        assertEquals("emerald_block", ValidatorStructure.lastLabel);
        assertSame(trigger, ValidatorStructure.lastTrigger);
    }

    @Test
    void structureParseUsesDefaultValueWhenOptionalEntryIsMissing() {
        Skript.registerStructure(
                ValidatorStructure.class,
                SyntaxInfo.Structure.NodeType.SECTION,
                VALIDATOR,
                "validated structure"
        );

        SectionNode node = new SectionNode("validated structure");
        SectionNode trigger = new SectionNode("trigger");
        node.add(trigger);

        org.skriptlang.skript.lang.structure.Structure parsed = org.skriptlang.skript.lang.structure.Structure.parse(
                "validated structure",
                node,
                "failed"
        );

        assertNotNull(parsed);
        assertEquals("fallback_block", ValidatorStructure.lastLabel);
        assertTrue(ValidatorStructure.lastTrigger != null);
    }

    public static final class ValidatorStructure extends org.skriptlang.skript.lang.structure.Structure {

        private static @Nullable String lastLabel;
        private static @Nullable SectionNode lastTrigger;

        @Override
        public boolean init(
                Literal<?>[] args,
                int matchedPattern,
                ParseResult parseResult,
                @Nullable EntryContainer entryContainer
        ) {
            if (entryContainer == null) {
                return false;
            }
            lastLabel = entryContainer.get("label", String.class, true);
            lastTrigger = entryContainer.get("trigger", SectionNode.class, false);
            return true;
        }

        @Override
        public boolean load() {
            return true;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "validated structure";
        }
    }
}
