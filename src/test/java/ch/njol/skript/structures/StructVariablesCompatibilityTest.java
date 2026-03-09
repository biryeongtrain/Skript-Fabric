package ch.njol.skript.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.Variables;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

class StructVariablesCompatibilityTest {

    @BeforeEach
    void setUp() {
        Skript.instance().syntaxRegistry().clearAll();
        Variables.clearAll();
        ParserInstance.get().setCurrentScript(new Script(null, List.of()));
        Skript.registerStructure(StructVariables.class, SyntaxInfo.Structure.NodeType.SECTION, "variables");
    }

    @AfterEach
    void tearDown() {
        Skript.instance().syntaxRegistry().clearAll();
        Variables.clearAll();
        ParserInstance.get().setCurrentScript(null);
    }

    @Test
    void variablesStructureParsesEqualsEntriesAndSeedsMissingGlobals() {
        SectionNode node = section(
                "variables",
                line("{joins} = 1"),
                line("{name} = \"alex\"")
        );

        Structure structure = Structure.parse("variables", node, null);

        assertInstanceOf(StructVariables.class, structure);
        assertTrue(structure.load());
        assertEquals(1, Variables.getVariable("joins", null, false));
        assertEquals("alex", Variables.getVariable("name", null, false));
    }

    @Test
    void variablesStructurePreservesExistingValuesWhenLoadingDefaults() {
        Variables.setVariable("joins", 5, null, false);
        SectionNode node = section(
                "variables",
                line("{joins} = 1"),
                line("{name} = \"alex\"")
        );

        Structure structure = Structure.parse("variables", node, null);

        assertInstanceOf(StructVariables.class, structure);
        assertTrue(structure.load());
        assertEquals(5, Variables.getVariable("joins", null, false));
        assertEquals("alex", Variables.getVariable("name", null, false));
    }

    private static SectionNode section(String key, SimpleNode... nodes) {
        SectionNode section = new SectionNode(key);
        for (SimpleNode node : nodes) {
            section.add(node);
        }
        return section;
    }

    private static SimpleNode line(String key) {
        return new SimpleNode(key);
    }
}
