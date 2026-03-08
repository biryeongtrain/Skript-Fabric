package ch.njol.skript.sections;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.EffectSectionEffect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SecIf extends EffectSection {

    private static final String[] PATTERNS = {
            "else",
            "else if any",
            "else if all",
            "else parse if <.+>",
            "else if <.+>",
            "if any",
            "if all",
            "parse if <.+>",
            "if <.+>",
            "then",
            "then run",
            "implicit:<.+>"
    };

    private @Nullable Condition condition;
    private List<Condition> multilineConditions = List.of();
    private ConditionalType type = ConditionalType.IF;
    private String source = "if";
    private boolean parseIf;
    private boolean parseIfPassed = true;
    private boolean multiline;
    private boolean ifAny;
    private boolean implicit;
    private @Nullable SecIf thenSection;

    public static void register() {
        Skript.registerSection(SecIf.class, PATTERNS);
    }

    private static @Nullable SecIf asConditional(@Nullable TriggerItem triggerItem) {
        if (triggerItem instanceof SecIf conditional) {
            return conditional;
        }
        if (triggerItem instanceof EffectSectionEffect effectSectionEffect
                && effectSectionEffect.effectSection() instanceof SecIf conditional) {
            return conditional;
        }
        return null;
    }

    private static @Nullable SecIf immediatelyPrecedingConditional(@Nullable List<TriggerItem> triggerItems) {
        if (triggerItems == null || triggerItems.isEmpty()) {
            return null;
        }
        return asConditional(triggerItems.get(triggerItems.size() - 1));
    }

    private static @Nullable SecIf closestPrecedingNonThenConditional(@Nullable List<TriggerItem> triggerItems) {
        if (triggerItems == null) {
            return null;
        }
        for (int index = triggerItems.size() - 1; index >= 0; index--) {
            SecIf conditional = asConditional(triggerItems.get(index));
            if (conditional == null) {
                return null;
            }
            if (conditional.type == ConditionalType.THEN) {
                continue;
            }
            return conditional;
        }
        return null;
    }

    private static boolean isThenSection(@Nullable Node node) {
        if (!(node instanceof SectionNode) || node.getKey() == null) {
            return false;
        }
        String key = ScriptLoader.replaceOptions(node.getKey()).trim();
        return key.equals("then") || key.equals("then run");
    }

    @Override
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        if (sectionNode == null) {
            return false;
        }

        type = switch (matchedPattern) {
            case 0 -> ConditionalType.ELSE;
            case 1, 2, 3, 4 -> ConditionalType.ELSE_IF;
            case 5, 6, 7, 8, 11 -> ConditionalType.IF;
            case 9, 10 -> ConditionalType.THEN;
            default -> throw new IllegalArgumentException("Unsupported conditional pattern: " + matchedPattern);
        };
        multiline = matchedPattern == 1 || matchedPattern == 2 || matchedPattern == 5 || matchedPattern == 6;
        ifAny = matchedPattern == 1 || matchedPattern == 5;
        parseIf = matchedPattern == 3 || matchedPattern == 7;
        implicit = parseResult.hasTag("implicit");

        String parsedSource = parseResult.expr;
        Node currentNode = ParserInstance.get().getNode();
        if ((parsedSource == null || parsedSource.isBlank()) && currentNode != null) {
            parsedSource = currentNode.getKey();
        }
        source = parsedSource == null ? "if" : parsedSource.trim();
        if (implicit) {
            source = "if " + source;
        }

        if (type == ConditionalType.THEN) {
            SecIf preceding = immediatelyPrecedingConditional(triggerItems);
            if (preceding == null || !preceding.multiline || preceding.type == ConditionalType.THEN) {
                Skript.error("'then' has to be placed just after a multiline 'if' or 'else if' section");
                return false;
            }
            preceding.thenSection = this;
            loadCode(sectionNode);
            return true;
        }

        if (type != ConditionalType.IF) {
            SecIf preceding = closestPrecedingNonThenConditional(triggerItems);
            if (preceding == null || preceding.type == ConditionalType.ELSE) {
                if (type == ConditionalType.ELSE_IF) {
                    Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
                } else {
                    Skript.error("'else' has to be placed just after another 'if' or 'else if' section");
                }
                return false;
            }
        } else if (multiline) {
            Node nextNode = currentNode == null ? null : currentNode.getParent() == null ? null : currentNode.getParent().getNext(currentNode);
            if (!isThenSection(nextNode)) {
                Skript.error((ifAny ? "'if any'" : "'if all'") + " has to be placed just before a 'then' section");
                return false;
            }
        }

        if (type != ConditionalType.ELSE) {
            if (multiline) {
                if (!parseMultilineConditions(sectionNode)) {
                    return false;
                }
            } else {
                if (parseResult.regexes.isEmpty()) {
                    return false;
                }
                String conditionText = parseResult.regexes.getFirst().group().trim();
                Condition parsedCondition = Condition.parse(conditionText, null);
                if (parsedCondition == null) {
                    return false;
                }
                condition = parsedCondition;
                if (parseIf) {
                    parseIfPassed = parsedCondition.check(ContextlessEvent.get());
                    if (!parseIfPassed) {
                        return true;
                    }
                }
            }
        }

        if (!multiline) {
            loadCode(sectionNode);
        }
        return true;
    }

    @Override
    public @Nullable TriggerItem getNext() {
        return getSkippedNext();
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        if (type == ConditionalType.THEN || (parseIf && !parseIfPassed)) {
            return getActualNext();
        }

        boolean passed = type == ConditionalType.ELSE || (parseIf ? parseIfPassed : checkConditions(event));
        debug(event, passed);
        if (!passed) {
            return getActualNext();
        }

        SecIf sectionToRun = multiline ? thenSection : this;
        TriggerItem skippedNext = getSkippedNext();
        if (sectionToRun != null && sectionToRun.last != null) {
            sectionToRun.last.setNext(skippedNext);
        }
        if (sectionToRun != null && sectionToRun.first != null) {
            return sectionToRun.first;
        }
        return skippedNext;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return source;
    }

    private boolean parseMultilineConditions(SectionNode sectionNode) {
        ParserInstance parser = ParserInstance.get();
        Node previousNode = parser.getNode();
        List<Condition> parsedConditions = new ArrayList<>();
        try {
            for (Node child : sectionNode) {
                if (child instanceof SectionNode) {
                    Skript.error((ifAny ? "'if any'" : "'if all'") + " sections may not contain other sections");
                    return false;
                }
                String childKey = child.getKey();
                if (childKey == null || childKey.isBlank()) {
                    continue;
                }
                parser.setNode(child);
                String parsedInput = ScriptLoader.replaceOptions(childKey.trim());
                Condition childCondition = Condition.parse(parsedInput, "Can't understand this condition: '" + parsedInput + "'");
                if (childCondition == null) {
                    return false;
                }
                parsedConditions.add(childCondition);
            }
        } finally {
            parser.setNode(previousNode);
        }

        if (parsedConditions.size() < 2) {
            Skript.error((ifAny ? "'if any'" : "'if all'") + " sections must contain at least two conditions");
            return false;
        }

        multilineConditions = parsedConditions;
        return true;
    }

    private boolean checkConditions(SkriptEvent event) {
        if (multiline) {
            if (ifAny) {
                for (Condition multilineCondition : multilineConditions) {
                    if (multilineCondition.check(event)) {
                        return true;
                    }
                }
                return false;
            }
            for (Condition multilineCondition : multilineConditions) {
                if (!multilineCondition.check(event)) {
                    return false;
                }
            }
            return true;
        }
        return condition != null && condition.check(event);
    }

    private @Nullable TriggerItem getSkippedNext() {
        TriggerItem next = getActualNext();
        while (true) {
            SecIf conditional = asConditional(next);
            if (conditional == null || conditional.type == ConditionalType.IF) {
                return next;
            }
            next = conditional.getActualNext();
        }
    }

    private enum ConditionalType {
        IF,
        ELSE_IF,
        ELSE,
        THEN
    }
}
