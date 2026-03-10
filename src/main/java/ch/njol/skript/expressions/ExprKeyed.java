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

@Name("Keyed")
@Description({
        "This expression is used to explicitly pass the keys of an expression alongside its values.",
        "For example, when setting a list variable or passing an expression to a function.",
})
@Example("""
	set {_first::foo} to "value1"
	set {_first::bar} to "value2"
	set {_second::*} to keyed {_first::*}
	# {_second::foo} is "value1" and {_second::bar} is "value2"
	""")
@Example("""
	function indices(objects: objects) returns strings:
		return indices of {_objects::*}

	on load:
		set {_list::foo} to "value1"
		set {_list::bar} to "value2"
		set {_list::baz} to "value3"

		broadcast indices({_list::*}) # "1", "2", "3"
		broadcast indices(keyed {_list::*}) # "foo", "bar", "baz"
	""")
@Example("""
	function plusOne(numbers: numbers) returns numbers:
		loop {_numbers::*}:
			set {_numbers::%loop-index%} to loop-value + 1
		return {_numbers::*}

	on load:
		set {_numbers::foo} to 1
		set {_numbers::bar} to 2
		set {_numbers::baz} to 3

		set {_result::*} to keyed plusOne(keyed {_numbers::*})
		# {_result::foo} is 2, {_result::bar} is 3, {_result::baz} is 4
	""")
@Since("2.12")
@Keywords("indexed")
public class ExprKeyed extends WrapperExpression<Object> implements KeyProviderExpression<Object> {

    static {
        Skript.registerExpression(ExprKeyed.class, Object.class, "(keyed|indexed) %~objects%");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!KeyProviderExpression.canReturnKeys(expressions[0])) {
            Skript.error(expressions[0] + " is not a keyed expression.");
            return false;
        }
        setExpr(expressions[0]);
        return true;
    }

    @Override
    public @NotNull String @NotNull [] getArrayKeys(SkriptEvent event) throws IllegalStateException {
        return getExpr().getArrayKeys(event);
    }

    @Override
    public @NotNull String @NotNull [] getAllKeys(SkriptEvent event) {
        return getExpr().getAllKeys(event);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
        return (Iterator) getExpr().keyedIterator(event);
    }

    @Override
    public boolean isIndexLoop(String input) {
        return getExpr().isIndexLoop(input);
    }

    @Override
    public boolean isLoopOf(String input) {
        return getExpr().isLoopOf(input);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "keyed " + getExpr().toString(event, debug);
    }

    @Override
    public KeyProviderExpression<?> getExpr() {
        return (KeyProviderExpression<?>) super.getExpr();
    }
}
