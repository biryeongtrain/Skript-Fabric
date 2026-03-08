package ch.njol.skript.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PatternCompilerCompatibilityTest {

    @Test
    void compiledPatternMatchesOptionalTaggedBranchOnlyWhenPresent() {
        SkriptPattern pattern = PatternCompiler.compile("root [optional:branch]");

        MatchResult withBranch = pattern.match("root branch");
        MatchResult withoutBranch = pattern.match("root");

        assertNotNull(withBranch);
        assertTrue(withBranch.tags().contains("optional"));
        assertNotNull(withoutBranch);
        assertFalse(withoutBranch.tags().contains("optional"));
    }

    @Test
    void compiledPatternXorsMatchedParseMarks() {
        SkriptPattern pattern = PatternCompiler.compile("1¦alpha 3¦beta");

        MatchResult result = pattern.match("alpha beta");

        assertNotNull(result);
        assertEquals(2, result.mark());
    }

    @Test
    void compiledPatternKeepsInlineOptionalWhitespaceNaturalForm() {
        SkriptPattern pattern = PatternCompiler.compile("can be (equipped|put) on[to] entities");

        MatchResult result = pattern.match("can be equipped on entities");

        assertNotNull(result);
    }
}
