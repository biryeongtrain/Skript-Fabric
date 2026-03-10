package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Recursive")
@Description("Returns all values of an expression, including those in nested structures such as lists of lists.")
@Example("""
	on load:
		set {_data::a::b::c} to "value1"
		set {_data::a::b::d} to "value2"
		set {_data::a::e} to "value3"
		set {_data::f} to "value4"

		broadcast recursive {_data::*}
		# broadcasts "value1", "value2", "value3", "value4"

		broadcast recursive indices of {_data::*}
		# broadcasts "a::b::c", "a::b::d", "a::e", "f"
	""")
@Since("2.14")
@Keywords({"deep", "nested"})
public class ExprRecursive extends WrapperExpression<Object> implements KeyProviderExpression<Object> {

    static {
        Skript.registerExpression(ExprRecursive.class, Object.class, "recursive %~objects%");
    }

    private boolean returnsKeys;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!expressions[0].returnNestedStructures(true)) {
            Skript.error(expressions[0] + " does not support nested structures.");
            return false;
        }
        setExpr(expressions[0]);
        returnsKeys = KeyProviderExpression.canReturnKeys(getExpr());
        return true;
    }

    @Override
    public Object[] getAll(SkriptEvent event) {
        return getExpr().getAll(event);
    }

    @Override
    public @NotNull String @NotNull [] getArrayKeys(SkriptEvent event) throws IllegalStateException {
        if (!returnsKeys) {
            throw new UnsupportedOperationException();
        }
        return getExpr().getArrayKeys(event);
    }

    @Override
    public @NotNull String @NotNull [] getAllKeys(SkriptEvent event) {
        if (!returnsKeys) {
            throw new UnsupportedOperationException();
        }
        return getExpr().getAllKeys(event);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
        if (!returnsKeys) {
            throw new UnsupportedOperationException();
        }
        return ((KeyProviderExpression<Object>) getExpr()).keyedIterator(event);
    }

    @Override
    public boolean canReturnKeys() {
        return returnsKeys;
    }

    @Override
    public boolean areKeysRecommended() {
        return KeyProviderExpression.areKeysRecommended(getExpr());
    }

    @Override
    public boolean isIndexLoop(String input) {
        if (!returnsKeys) {
            throw new IllegalStateException();
        }
        return getExpr().isIndexLoop(input);
    }

    @Override
    public boolean isLoopOf(String input) {
        return super.getExpr().isLoopOf(input);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "recursive " + super.getExpr().toString(event, debug);
    }

    @Override
    public KeyProviderExpression<?> getExpr() {
        return (KeyProviderExpression<?>) super.getExpr();
    }
}
