package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Name("Size")
@Description("""
	The size of something.
	Using 'size of {list::*}' will return the length of the list, so if you want the sizes of the things inside the \
	lists, use 'sizes of {list::*}'.
	""")
@Example("message \"There are %size of all players% players online!\"")
@Since({"1.0", "2.13 (sizes of)"})
public class PropExprSize extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	static {
		Skript.registerExpression(PropExprSize.class, Object.class,
			PropertyExpression.getPatterns("size[:s]", "objects"));
	}

	private ExpressionList<?> exprs;
	private boolean useProperties;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		useProperties = parseResult.hasTag("s") || expressions[0].isSingle();
		if (useProperties) {
			return super.init(expressions, matchedPattern, isDelayed, parseResult);
		} else {
			this.exprs = PropExprAmount.asExprList(expressions[0]);
			return LiteralUtils.canInitSafely(this.exprs);
		}
	}

	@Override
	protected Object @Nullable [] get(SkriptEvent event) {
		if (useProperties)
			return super.get(event);
		return new Long[]{(long) exprs.getArray(event).length};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (useProperties)
			return super.acceptChange(mode);
		return null;
	}

	@Override
	public @NotNull Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.SIZE;
	}

	@Override
	public boolean isSingle() {
		if (useProperties)
			return super.isSingle();
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		if (useProperties)
			return super.getReturnType();
		return Long.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		if (useProperties)
			return super.possibleReturnTypes();
		return new Class[]{Long.class};
	}

	@Override
	public String toString(SkriptEvent event, boolean debug) {
		if (useProperties)
			return super.toString(event, debug);
		return "size of " + this.exprs.toString(event, debug);
	}

}
