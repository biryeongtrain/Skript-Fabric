package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * A literal that has not yet been parsed to a concrete type.
 */
public class UnparsedLiteral implements Literal<Object> {

    private final String data;
    private final @Nullable LogEntry error;
    private final @Nullable List<ClassInfo<?>> possibleInfos;
    private boolean reparsed = false;
    private boolean converted = false;

    /**
     * @param data non-empty and trimmed string
     */
    public UnparsedLiteral(String data) {
        this(data, null);
    }

    /**
     * @param data non-empty and trimmed string
     * @param error error to log if this literal cannot be parsed
     */
    public UnparsedLiteral(String data, @Nullable LogEntry error) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("UnparsedLiteral data must not be blank");
        }
        this.data = data;
        this.error = error;
        this.possibleInfos = Classes.getPatternInfos(data);
    }

    public String getData() {
        return data;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public <R> @Nullable Literal<? extends R> getConvertedExpression(Class<R>... to) {
        return getConvertedExpression(ParseContext.DEFAULT, to);
    }

    public <R> @Nullable Literal<? extends R> getConvertedExpression(ParseContext context, Class<? extends R>... to) {
        if (to == null || to.length == 0) {
            return null;
        }
        ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            for (Class<? extends R> type : to) {
                R parsedObject = Classes.parse(data, type, context);
                if (parsedObject != null) {
                    if (!type.equals(Object.class)) {
                        converted = true;
                    }
                    log.printLog();
                    return new SimpleLiteral<>(parsedObject, false, this);
                }
                log.clear();
            }
            if (error != null) {
                log.printLog();
                SkriptLogger.log(error);
            } else {
                log.printError();
            }
            return null;
        } finally {
            log.stop();
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "'" + data + "'";
    }

    @Override
    public String toString() {
        return toString(null, false);
    }

    @Override
    public Expression<?> getSource() {
        return this;
    }

    @Override
    public boolean getAnd() {
        return true;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Expression<?> simplify() {
        return this;
    }

    public <T> @Nullable SimpleLiteral<T> reparse(Class<T> type) {
        T typedObject = Classes.parse(data, type, ParseContext.DEFAULT);
        if (typedObject != null) {
            if (!type.equals(Object.class)) {
                reparsed = true;
            }
            return new SimpleLiteral<>(typedObject, false, new UnparsedLiteral(data));
        }
        return null;
    }

    /**
     * @return true if this literal was successfully reparsed
     */
    public boolean wasReparsed() {
        return reparsed;
    }

    /**
     * @return true if this literal was successfully converted
     */
    public boolean wasConverted() {
        return converted;
    }

    /**
     * @return possible class infos this literal could parse to
     */
    public @Nullable List<ClassInfo<?>> getPossibleInfos() {
        return possibleInfos;
    }

    /**
     * Prints a warning when this literal can match multiple class infos.
     *
     * @return true if the warning was printed
     */
    public boolean multipleWarning() {
        if (reparsed || converted || possibleInfos == null || possibleInfos.size() <= 1) {
            return false;
        }
        String infoCodeName = possibleInfos.get(0).getCodeName();
        String combinedInfos = Classes.toString(possibleInfos.toArray(), true);
        Skript.warning("'" + data + "' has multiple types (" + combinedInfos + "). Consider specifying which type to use: '"
                + data + " (" + infoCodeName + ")'");
        return true;
    }

    private static SkriptAPIException invalidAccessException() {
        return new SkriptAPIException("UnparsedLiterals must be converted before use");
    }

    @Override
    public Object[] getAll(SkriptEvent event) {
        throw invalidAccessException();
    }

    @Override
    public Object[] getArray(SkriptEvent event) {
        throw invalidAccessException();
    }

    @Override
    public Object getSingle(SkriptEvent event) {
        throw invalidAccessException();
    }

    @Override
    public @Nullable java.util.Iterator<? extends Object> iterator(SkriptEvent event) {
        throw invalidAccessException();
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
        throw invalidAccessException();
    }

    @Override
    public Class<?>[] acceptChange(ChangeMode mode) {
        throw invalidAccessException();
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super Object> checker) {
        throw invalidAccessException();
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super Object> checker, boolean negated) {
        throw invalidAccessException();
    }

    @Override
    public boolean setTime(int time) {
        throw invalidAccessException();
    }

    @Override
    public int getTime() {
        throw invalidAccessException();
    }

    @Override
    public boolean isDefault() {
        throw invalidAccessException();
    }

    @Override
    public boolean isLoopOf(String input) {
        throw invalidAccessException();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        throw invalidAccessException();
    }
}
