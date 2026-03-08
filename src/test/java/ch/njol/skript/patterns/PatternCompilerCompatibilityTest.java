package ch.njol.skript.patterns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.SkriptParser;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
    void compiledPatternPreservesRepeatedTagOrder() {
        SkriptPattern pattern = PatternCompiler.compile("repeat:alpha unique:beta repeat:gamma");

        MatchResult result = pattern.match("alpha beta gamma");

        assertNotNull(result);
        assertEquals(List.of("repeat", "unique", "repeat"), result.tags());
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
    void compiledPatternLeavesOmittedPlaceholderCaptureNull() {
        SkriptPattern pattern = PatternCompiler.compile("default number [%number%]");

        MatchResult omitted = pattern.match("default number");
        MatchResult explicit = pattern.match("default number 5");

        assertNotNull(omitted);
        assertEquals(1, omitted.expressions().length);
        assertNull(omitted.expressions()[0]);

        assertNotNull(explicit);
        assertEquals(5, explicit.expressions()[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
    }

    @Test
    void compiledPatternKeepsSlashSeparatedPlaceholderUnionTypes() {
        SkriptPattern pattern = PatternCompiler.compile("value %integer/boolean%");

        TypePatternElement type = pattern.getElements(TypePatternElement.class).getFirst();

        assertArrayEquals(new Class<?>[]{Integer.class, Boolean.class}, type.returnTypes());
    }

    @Test
    void compiledPatternRejectsValuesOutsideSlashSeparatedPlaceholderUnion() {
        SkriptPattern pattern = PatternCompiler.compile("value %integer/boolean%");

        MatchResult integer = pattern.match("value 5");
        MatchResult bool = pattern.match("value true");
        MatchResult string = pattern.match("value hello");

        assertNotNull(integer);
        assertNotNull(bool);
        assertNull(string);
    }

    @Test
    void compiledPatternBuildsKeywordsForRequiredLiteralSurfaceOnly() {
        Keyword[] keywords = Keyword.buildKeywords(PatternCompiler.compilePattern("[the] (alpha|beta) gamma").first());

        assertEquals(2, keywords.length);
        assertTrue(Arrays.stream(keywords).allMatch(keyword -> keyword.isPresent("alpha gamma")));
        assertTrue(Arrays.stream(keywords).allMatch(keyword -> keyword.isPresent("beta gamma")));
        assertFalse(Arrays.stream(keywords).allMatch(keyword -> keyword.isPresent("the alpha")));
        assertFalse(Arrays.stream(keywords).allMatch(keyword -> keyword.isPresent("gamma")));
    }

    @Test
    void compiledPatternKeywordPrefilterKeepsOptionalNaturalFormMatches() {
        SkriptPattern pattern = PatternCompiler.compile("[the] name");

        assertNotNull(pattern.match("name"));
        assertNotNull(pattern.match("the name"));
        assertNull(pattern.match("the title"));
    }

    @Test
    void compiledPatternKeywordPrefilterKeepsGroupedChoiceMatchesWithTrailingLiteral() {
        SkriptPattern pattern = PatternCompiler.compile("(alpha|beta) gamma");

        assertNotNull(pattern.match("alpha gamma"));
        assertNotNull(pattern.match("beta gamma"));
        assertNull(pattern.match("alpha"));
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

    @Test
    void compiledPatternReconstructsNestedPatternElementStrings() {
        SkriptPattern pattern = PatternCompiler.compile("root [maybe (first|second)] tail");

        OptionalPatternElement optional = pattern.getElements(OptionalPatternElement.class).getFirst();
        ChoicePatternElement choice = pattern.getElements(ChoicePatternElement.class).getFirst();

        assertEquals("root [maybe (first|second)] tail", pattern.toString());
        assertEquals("[maybe (first|second)]", optional.toString());
        assertEquals("first|second", choice.toString());
    }

    @Test
    void compiledPatternExposesUpstreamStyleCombinationExpansion() {
        SkriptPattern pattern = PatternCompiler.compile("root [left|right] tag:value");

        OptionalPatternElement optional = pattern.getElements(OptionalPatternElement.class).getFirst();
        ParseTagPatternElement tag = pattern.getElements(ParseTagPatternElement.class).getFirst();

        assertEquals(Set.of("", "left", "right"), optional.getCombinations(true));
        assertEquals(Set.of(), tag.getCombinations(true));
        assertEquals(Set.of("tag:"), tag.getCombinations(false));
    }

    @Test
    void compiledPatternWrapsMalformedPatternsInUpstreamExceptionType() {
        MalformedPatternException exception = assertThrows(
                MalformedPatternException.class,
                () -> PatternCompiler.compile("broken <[>")
        );

        assertTrue(exception.getMessage().contains("[pattern: broken <[>]"));
    }

    @Test
    void compiledPatternExposesPlaceholderMetadata() {
        SkriptPattern pattern = PatternCompiler.compile("root %-*integer% then %strings@1%");

        TypePatternElement first = pattern.getElements(TypePatternElement.class).getFirst();
        TypePatternElement second = pattern.getElements(TypePatternElement.class).getLast();

        assertTrue(first.isOptional());
        assertEquals(SkriptParser.PARSE_LITERALS, first.flagMask());
        assertFalse(first.pluralities()[0]);
        assertFalse(second.isOptional());
        assertEquals(SkriptParser.ALL_FLAGS, second.flagMask());
        assertTrue(second.pluralities()[0]);
        assertEquals(1, second.time());
    }
}
