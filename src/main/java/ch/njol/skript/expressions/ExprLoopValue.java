package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Loop value")
@Description("Returns the previous, current, or next looped value.")
@Example("""
    loop {top-balances::*}:
        if loop-iteration <= 10:
            broadcast "#%loop-iteration% %loop-index% has $%loop-value%"
    """)
@Since("1.0, 2.10 (previous, next), Fabric")
public class ExprLoopValue extends SimpleExpression<Object> {

    enum LoopState {
        CURRENT("[current]"),
        NEXT("next"),
        PREVIOUS("previous");

        private final String pattern;

        LoopState(String pattern) {
            this.pattern = pattern;
        }
    }

    private static final LoopState[] LOOP_STATES = LoopState.values();
    private static final Pattern LOOP_PATTERN = Pattern.compile("^(.+)-(\\d+)$");

    static {
        String[] patterns = new String[LOOP_STATES.length];
        for (LoopState state : LOOP_STATES) {
            patterns[state.ordinal()] = "[the] " + state.pattern + " loop-<.+>";
        }
        Skript.registerExpression(ExprLoopValue.class, Object.class, patterns);
    }

    private String name;
    private SecLoop loop;
    boolean isKeyedLoop;
    boolean isIndex;
    private LoopState selectedState;

    @Override
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        selectedState = LOOP_STATES[matchedPattern];
        name = parser.expr;
        String loopOf = parser.regexes.get(0).group();
        int expectedDepth = -1;
        Matcher matcher = LOOP_PATTERN.matcher(loopOf);
        if (matcher.matches()) {
            loopOf = matcher.group(1);
            expectedDepth = Utils.parseInt(matcher.group(2));
        }

        if ("counter".equalsIgnoreCase(loopOf) || "iteration".equalsIgnoreCase(loopOf)) {
            return false;
        }

        ClassInfo<?> expectedInfo = Classes.getClassInfoFromUserInput(loopOf);
        Class<?> expectedClass = expectedInfo == null ? null : expectedInfo.getC();
        int candidateDepth = 1;
        SecLoop matchedLoop = null;

        for (SecLoop candidate : getParser().getCurrentSections(SecLoop.class)) {
            if ((expectedClass != null && expectedClass.isAssignableFrom(candidate.getLoopedExpression().getReturnType()))
                    || "value".equalsIgnoreCase(loopOf)
                    || candidate.getLoopedExpression().isLoopOf(loopOf)) {
                if (candidateDepth < expectedDepth) {
                    candidateDepth++;
                    continue;
                }
                if (matchedLoop != null) {
                    Skript.error("There are multiple loops that match loop-" + loopOf + ". Use loop-" + loopOf + "-1/2/3/etc. to specify which loop's value you want.");
                    return false;
                }
                matchedLoop = candidate;
                if (candidateDepth == expectedDepth) {
                    break;
                }
            }
        }
        if (matchedLoop == null) {
            Skript.error("There's no loop that matches 'loop-" + loopOf + "'");
            return false;
        }
        if (selectedState == LoopState.NEXT && !matchedLoop.supportsPeeking()) {
            Skript.error("The expression '" + matchedLoop.getExpression().toString() + "' does not allow the usage of 'next loop-" + loopOf + "'.");
            return false;
        }
        if (matchedLoop.isKeyedLoop()) {
            isKeyedLoop = true;
            if (((KeyedIterableExpression<?>) matchedLoop.getLoopedExpression()).isIndexLoop(loopOf)) {
                isIndex = true;
            }
        }
        loop = matchedLoop;
        return true;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        if (isIndex) {
            return String.class;
        }
        return loop.getLoopedExpression().getReturnType();
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        if (isIndex) {
            return new Class[]{String.class};
        }
        return loop.getLoopedExpression().possibleReturnTypes();
    }

    @Override
    public boolean canReturn(Class<?> returnType) {
        if (isIndex) {
            return super.canReturn(returnType);
        }
        return loop.getLoopedExpression().canReturn(returnType);
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        if (isKeyedLoop) {
            @SuppressWarnings("unchecked")
            KeyedValue<Object> value = (KeyedValue<Object>) switch (selectedState) {
                case CURRENT -> loop.getCurrent(event);
                case NEXT -> loop.getNext(event);
                case PREVIOUS -> loop.getPrevious(event);
            };
            if (value == null) {
                return null;
            }
            if (isIndex) {
                return new String[]{value.key()};
            }
            Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
            one[0] = value.value();
            return one;
        }

        Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
        one[0] = switch (selectedState) {
            case CURRENT -> loop.getCurrent(event);
            case NEXT -> loop.getNext(event);
            case PREVIOUS -> loop.getPrevious(event);
        };
        return one;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (event == null) {
            return name;
        }
        if (isKeyedLoop) {
            @SuppressWarnings("unchecked")
            KeyedValue<Object> value = (KeyedValue<Object>) switch (selectedState) {
                case CURRENT -> loop.getCurrent(event);
                case NEXT -> loop.getNext(event);
                case PREVIOUS -> loop.getPrevious(event);
            };
            if (value == null) {
                return String.valueOf((Object) null);
            }
            return isIndex ? "\"" + value.key() + "\"" : String.valueOf(value.value());
        }
        return String.valueOf(switch (selectedState) {
            case CURRENT -> loop.getCurrent(event);
            case NEXT -> loop.getNext(event);
            case PREVIOUS -> loop.getPrevious(event);
        });
    }
}
