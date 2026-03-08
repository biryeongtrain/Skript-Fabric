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

    @Test
    void compiledPatternAutoDerivesLeadingLiteralTag() {
        SkriptPattern pattern = PatternCompiler.compile("[:non(-| )]op[erator]s");

        MatchResult hyphenated = pattern.match("non-operators");
        MatchResult spaced = pattern.match("non operators");
        MatchResult plain = pattern.match("operators");

        assertNotNull(hyphenated);
        assertTrue(hyphenated.tags().contains("non"));
        assertNotNull(spaced);
        assertTrue(spaced.tags().contains("non"));
        assertNotNull(plain);
        assertFalse(plain.tags().contains("non"));
    }

    @Test
    void compiledPatternAutoDerivesChoiceBranchTags() {
        SkriptPattern pattern = PatternCompiler.compile("[:(min|max)[imum]] [sea] pickle(s| (count|amount))");

        MatchResult minimum = pattern.match("minimum sea pickle count");
        MatchResult maximum = pattern.match("maximum sea pickles");
        MatchResult plain = pattern.match("sea pickles");

        assertNotNull(minimum);
        assertTrue(minimum.tags().contains("min"));
        assertNotNull(maximum);
        assertTrue(maximum.tags().contains("max"));
        assertNotNull(plain);
        assertFalse(plain.tags().contains("min"));
        assertFalse(plain.tags().contains("max"));
    }

    @Test
    void compiledPatternAutoDerivedTagsAreLowerCasedFromLiteralBranches() {
        SkriptPattern pattern = PatternCompiler.compile("hash with (:(MD5|SHA-256|SHA-384|SHA-512))");

        MatchResult result = pattern.match("hash with SHA-256");

        assertNotNull(result);
        assertTrue(result.tags().contains("sha-256"));
    }

    @Test
    void compiledPatternSkipsOptionalRegexCaptureWhenOmitted() {
        SkriptPattern pattern = PatternCompiler.compile("show [<.+>] result");

        MatchResult omitted = pattern.match("show result");
        MatchResult present = pattern.match("show captured result");

        assertNotNull(omitted);
        assertTrue(omitted.regexes().isEmpty());
        assertNotNull(present);
        assertEquals("captured", present.regexes().getFirst().group());
    }

    @Test
    void compiledPatternSkipsRegexCapturesFromUnmatchedAlternationBranches() {
        SkriptPattern pattern = PatternCompiler.compile("check (<\\d+>|literal)");

        MatchResult literal = pattern.match("check literal");
        MatchResult regex = pattern.match("check 42");

        assertNotNull(literal);
        assertTrue(literal.regexes().isEmpty());
        assertNotNull(regex);
        assertEquals("42", regex.regexes().getFirst().group());
    }

    @Test
    void compiledPatternExposesTypeCountsAcrossAlternationBranches() {
        SkriptPattern pattern = PatternCompiler.compile("(first %string%|second %number% %boolean%)");

        assertEquals(3, pattern.countTypes());
        assertEquals(2, pattern.countNonNullTypes());
    }

    @Test
    void compiledPatternExposesPatternElementGraph() {
        SkriptPattern pattern = PatternCompiler.compile("root [maybe %number%] (first %string%|second %boolean% %string%)");

        assertEquals(4, pattern.getElements(TypePatternElement.class).size());
        assertEquals(1, pattern.getElements(OptionalPatternElement.class).size());
        assertEquals(1, pattern.getElements(ChoicePatternElement.class).size());
        assertEquals(0, pattern.getElements(TypePatternElement.class).getFirst().expressionIndex());
        assertEquals(3, pattern.getElements(TypePatternElement.class).getLast().expressionIndex());
    }
}
