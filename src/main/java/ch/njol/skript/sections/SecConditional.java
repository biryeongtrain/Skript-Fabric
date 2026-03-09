package ch.njol.skript.sections;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class SecConditional extends Section {

    private static final SkriptPattern THEN_PATTERN = PatternCompiler.compile("then [run]");
    private static final Patterns<ConditionalType> CONDITIONAL_PATTERNS = new Patterns<>(new Object[][]{
            {"else", ConditionalType.ELSE},
            {"else [:parse] if <.+>", ConditionalType.ELSE_IF},
            {"else [:parse] if (:any|any:at least one [of])", ConditionalType.ELSE_IF},
            {"else [:parse] if [all]", ConditionalType.ELSE_IF},
            {"[:parse] if (:any|any:at least one [of])", ConditionalType.IF},
            {"[:parse] if [all]", ConditionalType.IF},
            {"[:parse] if <.+>", ConditionalType.IF},
            {THEN_PATTERN.toString(), ConditionalType.THEN},
            {"implicit:<.+>", ConditionalType.IF}
    });

    private enum ConditionalType {
        ELSE, ELSE_IF, IF, THEN
    }

    private ConditionalType type;
    private @Nullable Condition condition;
    private List<Condition> multilineConditions = List.of();
    private boolean ifAny;
    private boolean parseIf;
    private boolean parseIfPassed;
    private boolean multiline;

    private @Nullable Kleenean hasDelayBefore;
    private @Nullable Kleenean shouldDelayAfter;
    private @Nullable ExecutionIntent executionIntent;

    public static void register() {
        Skript.registerSection(SecConditional.class, CONDITIONAL_PATTERNS.getPatterns());
    }

    @Override
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        if (sectionNode == null || triggerItems == null) {
            return false;
        }

        type = CONDITIONAL_PATTERNS.getInfo(matchedPattern);
        ifAny = parseResult.hasTag("any");
        parseIf = parseResult.hasTag("parse");
        multiline = parseResult.regexes.isEmpty() && type != ConditionalType.ELSE;
        ParserInstance parser = getParser();

        if (type != ConditionalType.IF) {
            if (type == ConditionalType.THEN) {
                SecConditional precedingConditional = getPrecedingConditional(triggerItems, null);
                if (precedingConditional == null || !precedingConditional.multiline) {
                    Skript.error("'then' has to placed just after a multiline 'if' or 'else if' section");
                    return false;
                }
            } else {
                SecConditional precedingIf = getPrecedingConditional(triggerItems, ConditionalType.IF);
                if (precedingIf == null) {
                    if (type == ConditionalType.ELSE_IF) {
                        Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
                    } else {
                        Skript.error("'else' has to be placed just after another 'if' or 'else if' section");
                    }
                    return false;
                }
            }
        } else if (multiline) {
            Node nextNode = getNextNode(sectionNode, parser);
            String error = (ifAny ? "'if any'" : "'if all'") + " has to be placed just before a 'then' section";
            if (nextNode instanceof SectionNode && nextNode.getKey() != null) {
                String nextNodeKey = ScriptLoader.replaceOptions(nextNode.getKey());
                if (THEN_PATTERN.match(nextNodeKey) == null) {
                    Skript.error(error);
                    return false;
                }
            } else {
                Skript.error(error);
                return false;
            }
            hasDelayBefore = parser.getHasDelayBefore();
        } else {
            hasDelayBefore = parser.getHasDelayBefore();
        }

        if (!parser.getHasDelayBefore().isTrue()) {
            Kleenean wasDelayedBeforeChain = hasDelayBefore != null ? hasDelayBefore
                    : getPrecedingConditional(triggerItems, ConditionalType.IF).hasDelayBefore;
            assert wasDelayedBeforeChain != null;
            parser.setHasDelayBefore(wasDelayedBeforeChain);
        }

        if (type == ConditionalType.IF || type == ConditionalType.ELSE_IF) {
            Class<?>[] currentEvents = parser.getCurrentEventClasses();
            String currentEventName = parser.getCurrentEventName();
            List<Condition> conditionals = new ArrayList<>();

            if (parseIf) {
                parser.setCurrentEvent("parse", ContextlessEvent.class);
            }

            if (multiline) {
                int nonEmptyNodeCount = Iterables.size(sectionNode);
                if (nonEmptyNodeCount < 2) {
                    Skript.error((ifAny ? "'if any'" : "'if all'") + " sections must contain at least two conditions");
                    return false;
                }
                for (Node childNode : sectionNode) {
                    if (childNode instanceof SectionNode) {
                        Skript.error((ifAny ? "'if any'" : "'if all'") + " sections may not contain other sections");
                        return false;
                    }
                    String childKey = childNode.getKey();
                    if (childKey != null) {
                        childKey = ScriptLoader.replaceOptions(childKey);
                        parser.setNode(childNode);
                        Condition childCondition = Condition.parse(
                                childKey,
                                "Can't understand this condition: '" + childKey + "'"
                        );
                        if (childCondition == null) {
                            return false;
                        }
                        conditionals.add(childCondition);
                    }
                }
                parser.setNode(sectionNode);
            } else {
                String expr = parseResult.regexes.getFirst().group();
                Condition parsed = Condition.parse(
                        expr,
                        parseResult.hasTag("implicit") ? null : "Can't understand this condition: '" + expr + "'"
                );
                if (parsed != null) {
                    conditionals.add(parsed);
                }
            }

            if (parseIf) {
                if (currentEventName == null) {
                    parser.deleteCurrentEvent();
                } else {
                    parser.setCurrentEvent(currentEventName, currentEvents);
                }
            }

            if (conditionals.isEmpty()) {
                return false;
            }

            if (multiline) {
                multilineConditions = conditionals;
            } else {
                condition = conditionals.getFirst();
            }
        }

        if (parseIf) {
            parseIfPassed = checkConditions(ContextlessEvent.get());
            if (!parseIfPassed) {
                return true;
            }
        }

        if (!multiline || type == ConditionalType.THEN) {
            boolean considerDelayUpdate = !parser.getHasDelayBefore().isTrue();
            loadCode(sectionNode);

            if (considerDelayUpdate) {
                Kleenean hasDelayAfter = parser.getHasDelayBefore();
                Kleenean preceding = getPrecedingShouldDelayAfter(triggerItems);
                if (preceding == null || preceding == hasDelayAfter) {
                    shouldDelayAfter = hasDelayAfter;
                } else {
                    shouldDelayAfter = Kleenean.UNKNOWN;
                }
                if (shouldDelayAfter.isTrue()) {
                    parser.setHasDelayBefore(type == ConditionalType.ELSE ? Kleenean.TRUE : Kleenean.UNKNOWN);
                } else {
                    parser.setHasDelayBefore(shouldDelayAfter);
                }
            }
        }

        if (type == ConditionalType.ELSE) {
            List<SecConditional> conditionals = getPrecedingConditionals(triggerItems);
            conditionals.addFirst(this);
            for (SecConditional current : conditionals) {
                if (current.multiline && current.type != ConditionalType.THEN) {
                    continue;
                }
                ExecutionIntent triggerIntent = current.triggerExecutionIntent();
                if (triggerIntent == null) {
                    executionIntent = null;
                    break;
                }
                if (executionIntent == null || triggerIntent.compareTo(executionIntent) < 0) {
                    executionIntent = triggerIntent;
                }
            }
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
        } else if (parseIf || checkConditions(event)) {
            SecConditional sectionToRun = multiline ? (SecConditional) getActualNext() : this;
            TriggerItem skippedNext = getSkippedNext();
            if (sectionToRun.last != null) {
                sectionToRun.last.setNext(skippedNext);
            }
            return sectionToRun.first != null ? sectionToRun.first : skippedNext;
        } else {
            return getActualNext();
        }
    }

    @Override
    public @Nullable ExecutionIntent executionIntent() {
        return executionIntent;
    }

    @Override
    public ExecutionIntent triggerExecutionIntent() {
        if (multiline && type != ConditionalType.THEN) {
            return null;
        }
        return super.triggerExecutionIntent();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String parsePrefix = parseIf ? "parse " : "";
        return switch (type) {
            case IF -> multiline ? parsePrefix + "if " + (ifAny ? "any" : "all")
                    : parsePrefix + "if " + condition.toString(event, debug);
            case ELSE_IF -> multiline ? "else " + parsePrefix + "if " + (ifAny ? "any" : "all")
                    : "else " + parsePrefix + "if " + condition.toString(event, debug);
            case ELSE -> "else";
            case THEN -> "then";
        };
    }

    private @Nullable TriggerItem getSkippedNext() {
        TriggerItem next = getActualNext();
        while (next instanceof SecConditional nextConditional && nextConditional.type != ConditionalType.IF) {
            next = next.getActualNext();
        }
        return next;
    }

    private static @Nullable SecConditional getPrecedingConditional(
            List<TriggerItem> triggerItems,
            @Nullable ConditionalType type
    ) {
        for (int i = triggerItems.size() - 1; i >= 0; i--) {
            TriggerItem triggerItem = triggerItems.get(i);
            if (triggerItem instanceof SecConditional preceding) {
                if (preceding.type == ConditionalType.ELSE) {
                    return null;
                }
                if (type == null || preceding.type == type) {
                    return preceding;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private static List<SecConditional> getPrecedingConditionals(List<TriggerItem> triggerItems) {
        List<SecConditional> conditionals = new ArrayList<>();
        for (int i = triggerItems.size() - 1; i >= 0; i--) {
            TriggerItem triggerItem = triggerItems.get(i);
            if (!(triggerItem instanceof SecConditional conditional)) {
                break;
            }
            if (conditional.type == ConditionalType.ELSE) {
                break;
            }
            conditionals.add(conditional);
        }
        return conditionals;
    }

    private static @Nullable Kleenean getPrecedingShouldDelayAfter(List<TriggerItem> triggerItems) {
        for (int i = triggerItems.size() - 1; i >= 0; i--) {
            TriggerItem triggerItem = triggerItems.get(i);
            if (!(triggerItem instanceof SecConditional conditional)) {
                break;
            }
            if (conditional.type == ConditionalType.ELSE) {
                break;
            }
            if (conditional.shouldDelayAfter != null) {
                return conditional.shouldDelayAfter;
            }
        }
        return null;
    }

    private boolean checkConditions(SkriptEvent event) {
        if (multiline) {
            if (ifAny) {
                for (Condition child : multilineConditions) {
                    if (child.check(event)) {
                        return true;
                    }
                }
                return false;
            }
            for (Condition child : multilineConditions) {
                if (!child.check(event)) {
                    return false;
                }
            }
            return true;
        }
        return condition == null || condition.check(event);
    }

    private @Nullable Node getNextNode(Node precedingNode, ParserInstance parser) {
        Node originalCurrentNode = parser.getNode();
        SectionNode parentNode = precedingNode.getParent();
        if (parentNode == null) {
            return null;
        }
        Iterator<Node> parentIterator = parentNode.iterator();
        while (parentIterator.hasNext()) {
            Node current = parentIterator.next();
            if (current == precedingNode) {
                Node nextNode = parentIterator.hasNext() ? parentIterator.next() : null;
                parser.setNode(originalCurrentNode);
                return nextNode;
            }
        }
        parser.setNode(originalCurrentNode);
        return null;
    }
}
