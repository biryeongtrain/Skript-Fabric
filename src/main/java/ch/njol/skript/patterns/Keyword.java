package ch.njol.skript.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

abstract class Keyword {

    abstract boolean isPresent(String expr);

    static Keyword[] buildKeywords(PatternElement first) {
        return buildKeywords(first, true, 0, false);
    }

    private static Keyword[] buildKeywords(
            PatternElement first,
            boolean starting,
            int depth,
            boolean hasFollowingContentOutsideSequence
    ) {
        List<Keyword> keywords = new ArrayList<>();
        PatternElement next = first;
        while (next != null) {
            boolean hasFollowingContent = hasFollowingContentOutsideSequence || hasPatternContent(next.getOriginalNext());
            if (next instanceof LiteralPatternElement literalPattern) {
                String literal = PatternCompiler.normalizeInput(literalPattern.literal()).toLowerCase(Locale.ENGLISH);
                if (!literal.isEmpty()) {
                    keywords.add(new SimpleKeyword(literal, starting, !hasFollowingContent));
                }
            } else if (depth <= 1 && next instanceof ChoicePatternElement choice) {
                Set<Set<Keyword>> choices = new LinkedHashSet<>();
                for (PatternElement branch : choice.getPatternElements()) {
                    Set<Keyword> branchKeywords = new LinkedHashSet<>(Arrays.asList(
                            buildKeywords(branch, starting, depth, hasFollowingContent)
                    ));
                    if (branchKeywords.isEmpty()) {
                        choices.clear();
                        break;
                    }
                    choices.add(branchKeywords);
                }
                if (!choices.isEmpty()) {
                    keywords.add(new ChoiceKeyword(choices));
                }
            } else if (next instanceof GroupPatternElement group) {
                Collections.addAll(
                        keywords,
                        buildKeywords(group.getPatternElement(), starting, depth + 1, hasFollowingContent)
                );
            }

            if (!(next instanceof ParseTagPatternElement)) {
                starting = false;
            }
            next = next.getOriginalNext();
        }
        return keywords.toArray(Keyword[]::new);
    }

    private static boolean hasPatternContent(PatternElement element) {
        PatternElement next = element;
        while (next != null) {
            if (!(next instanceof ParseTagPatternElement)) {
                return true;
            }
            next = next.getOriginalNext();
        }
        return false;
    }

    private static final class SimpleKeyword extends Keyword {

        private final String keyword;
        private final boolean starting;
        private final boolean ending;

        private SimpleKeyword(String keyword, boolean starting, boolean ending) {
            this.keyword = keyword;
            this.starting = starting;
            this.ending = ending;
        }

        @Override
        boolean isPresent(String expr) {
            if (starting) {
                return expr.startsWith(keyword);
            }
            if (ending) {
                return expr.endsWith(keyword);
            }
            return expr.contains(keyword);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyword, starting, ending);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof SimpleKeyword other)) {
                return false;
            }
            return keyword.equals(other.keyword) && starting == other.starting && ending == other.ending;
        }

        @Override
        public String toString() {
            return "SimpleKeyword[keyword=" + keyword + ", starting=" + starting + ", ending=" + ending + "]";
        }
    }

    private static final class ChoiceKeyword extends Keyword {

        private final Set<Set<Keyword>> choices;

        private ChoiceKeyword(Set<Set<Keyword>> choices) {
            this.choices = Set.copyOf(choices);
        }

        @Override
        boolean isPresent(String expr) {
            return choices.stream().anyMatch(keywords -> keywords.stream().allMatch(keyword -> keyword.isPresent(expr)));
        }

        @Override
        public int hashCode() {
            return Objects.hash(choices);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof ChoiceKeyword other)) {
                return false;
            }
            return choices.equals(other.choices);
        }

        @Override
        public String toString() {
            return "ChoiceKeyword[choices=" + choices.stream()
                    .map(Collection::toString)
                    .toList() + "]";
        }
    }
}
