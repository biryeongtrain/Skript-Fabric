package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprNumbers;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.ContainerExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.PeekingIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Loop")
@Description({
        "Loop sections repeat their code with multiple values.",
        "A loop will loop through all elements of the given expression."
})
@Example("""
        loop all players:
            send "Hello %loop-player%!" to loop-player
        """)
@Example("""
        loop 10 times:
            broadcast "%loop-value%"
        """)
@Since("1.0")
public class SecLoop extends LoopSection {

    private static final Pattern EXPR_NUMBERS_PATTERN = Pattern.compile(
            "^(?:(?:all(?: of)? the|the) )?(numbers|integers|decimals) (?:between|from) (.+?) (?:and|to) (.+)$",
            Pattern.CASE_INSENSITIVE
    );

    static {
        Skript.registerSection(SecLoop.class, "loop %objects%");
    }

    protected @UnknownNullability Expression<?> expression;

    private final transient Map<SkriptEvent, Object> current = new WeakHashMap<>();
    private final transient Map<SkriptEvent, Iterator<?>> iteratorMap = new WeakHashMap<>();
    private final transient Map<SkriptEvent, Object> previous = new WeakHashMap<>();

    protected @Nullable TriggerItem actualNext;
    private boolean guaranteedToLoop;
    private @Nullable Object nextValue;
    private boolean loopPeeking;
    protected boolean iterableSingle;
    protected boolean keyed;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(
            Expression<?>[] exprs,
            int matchedPattern,
            Kleenean isDelayed,
            ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        if (sectionNode == null) {
            return false;
        }

        expression = LiteralUtils.defendExpression(exprs[0]);
        if (!LiteralUtils.canInitSafely(expression)) {
            Skript.error("Can't understand this loop: '" + parseResult.expr.substring(5) + "'");
            return false;
        }
        Expression<?> recoveredExpression = recoverKnownLoopExpression(expression);
        if (recoveredExpression != null) {
            expression = recoveredExpression;
        }

        if (!(expression instanceof Variable) && Container.class.isAssignableFrom(expression.getReturnType())) {
            Container.ContainerType type = expression.getReturnType().getAnnotation(Container.ContainerType.class);
            if (type == null) {
                throw new SkriptAPIException(expression.getReturnType().getName()
                        + " implements Container but is missing the required @ContainerType annotation");
            }
            expression = new ContainerExpression((Expression<? extends Container<?>>) expression, type.value());
        }

        if (expression.isSingle()
                && (expression instanceof Variable<?> || expression.canReturn(Iterable.class))) {
            iterableSingle = true;
        } else if (expression.isSingle()) {
            Skript.error("Can't loop '" + expression + "' because it's only a single value");
            return false;
        }

        loopPeeking = exprs[0].supportsLoopPeeking();
        guaranteedToLoop = guaranteedToLoop(expression);
        keyed = KeyedIterableExpression.canIterateWithKeys(expression);
        loadOptionalCode(sectionNode);
        setInternalNext(this);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        Iterator<?> iterator = iteratorMap.get(event);
        if (iterator == null) {
            if (iterableSingle) {
                Object value = expression.getSingle(event);
                if (value instanceof Container<?> container) {
                    iterator = container.containerIterator();
                } else if (value instanceof Iterable<?> iterable) {
                    iterator = iterable.iterator();
                } else {
                    iterator = Collections.singleton(value).iterator();
                }
            } else {
                iterator = keyed
                        ? ((KeyedIterableExpression<?>) expression).keyedIterator(event)
                        : expression.iterator(event);
                if (iterator != null && iterator.hasNext()) {
                    iteratorMap.put(event, iterator);
                } else {
                    iterator = null;
                }
            }
        }

        if (iterator == null || (!iterator.hasNext() && nextValue == null)) {
            exit(event);
            debug(event, false);
            return actualNext;
        }

        previous.put(event, current.get(event));
        if (nextValue != null) {
            store(event, nextValue);
            nextValue = null;
        } else {
            store(event, iterator.next());
        }
        return walk(event, true);
    }

    protected void store(SkriptEvent event, Object next) {
        current.put(event, next);
        currentLoopCounter.put(event, currentLoopCounter.getOrDefault(event, 0L) + 1);
    }

    @Override
    public @Nullable ExecutionIntent executionIntent() {
        return guaranteedToLoop ? triggerExecutionIntent() : null;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "loop " + expression.toString(event, debug);
    }

    public @Nullable Object getCurrent(SkriptEvent event) {
        return current.get(event);
    }

    public @Nullable Object getNext(SkriptEvent event) {
        if (!loopPeeking) {
            return null;
        }
        Iterator<?> iterator = iteratorMap.get(event);
        if (iterator == null || !iterator.hasNext()) {
            return null;
        }
        if (iterator instanceof PeekingIterator<?> peekingIterator) {
            return peekingIterator.peek();
        }
        nextValue = iterator.next();
        return nextValue;
    }

    public @Nullable Object getPrevious(SkriptEvent event) {
        return previous.get(event);
    }

    public Expression<?> getLoopedExpression() {
        return expression;
    }

    public boolean isKeyedLoop() {
        return keyed;
    }

    @Override
    public SecLoop setNext(@Nullable TriggerItem next) {
        actualNext = next;
        return this;
    }

    protected void setInternalNext(TriggerItem item) {
        super.setNext(item);
    }

    @Override
    public @Nullable TriggerItem getActualNext() {
        return actualNext;
    }

    @Override
    public void exit(SkriptEvent event) {
        current.remove(event);
        iteratorMap.remove(event);
        previous.remove(event);
        nextValue = null;
        super.exit(event);
    }

    public boolean supportsPeeking() {
        return loopPeeking;
    }

    public Expression<?> getExpression() {
        return expression;
    }

    private static boolean guaranteedToLoop(Expression<?> expression) {
        if (expression instanceof Literal<?> literal) {
            return literal.getAll(SkriptEvent.EMPTY).length > 0;
        }
        if (!(expression instanceof ExpressionList<?> list)) {
            return false;
        }
        if (!list.getAnd()) {
            for (Expression<?> expr : list.getExpressions()) {
                if (!guaranteedToLoop(expr)) {
                    return false;
                }
            }
            return true;
        }
        for (Expression<?> expr : list.getExpressions()) {
            if (guaranteedToLoop(expr)) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable Expression<?> recoverKnownLoopExpression(@Nullable Expression<?> candidate) {
        if (candidate == null || !candidate.isSingle()) {
            return null;
        }
        Object raw = candidate.getSingle(SkriptEvent.EMPTY);
        if (!(raw instanceof String text)) {
            return null;
        }
        Matcher matcher = EXPR_NUMBERS_PATTERN.matcher(text.trim());
        if (!matcher.matches()) {
            return null;
        }

        Number start = parseLoopNumber(matcher.group(2).trim());
        Number end = parseLoopNumber(matcher.group(3).trim());
        if (start == null || end == null) {
            return null;
        }

        int mode = switch (matcher.group(1).toLowerCase(java.util.Locale.ENGLISH)) {
            case "integers" -> 1;
            case "decimals" -> 2;
            default -> 0;
        };

        ExprNumbers numbers = new ExprNumbers();
        ch.njol.skript.lang.SkriptParser.ParseResult parseResult = new ch.njol.skript.lang.SkriptParser.ParseResult();
        parseResult.expr = text;
        if (!numbers.init(
                new Expression[]{
                        new SimpleLiteral<>(start, false),
                        new SimpleLiteral<>(end, false)
                },
                mode,
                Kleenean.FALSE,
                parseResult
        )) {
            return null;
        }
        return numbers;
    }

    private static @Nullable Number parseLoopNumber(String input) {
        try {
            return Double.parseDouble(input.replace("_", ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
