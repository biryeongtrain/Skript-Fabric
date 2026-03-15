package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprArgument;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Name("Arithmetic")
@Description("Arithmetic expressions, e.g. 1 + 2, (health of player - 2) / 3, etc.")
@Example("set the player's health to 10 - the player's health")
@Example("""
    loop (argument + 2) / 5 times:
    	message "Two useless numbers: %loop-num * 2 - 5%, %2^loop-num - 1%"
    """)
@Example("message \"You have %health of player * 2% half hearts of HP!\"")
@Since("1.4.2")
@SuppressWarnings("null")
public class ExprArithmetic<L, R, T> extends SimpleExpression<T> {

	private record PatternInfo(Operator operator, boolean leftGrouped, boolean rightGrouped) {
	}

	// initialized during registration
	private static Patterns<PatternInfo> patterns = null;

	public static void registerExpression() {
		Skript.checkAcceptRegistrations();
		List<Object[]> infos = new ArrayList<>();
		for (Operator operator : Arithmetics.getAllOperators()) {
			infos.add(new Object[] {"\\(%object%\\)[ ]" + operator.sign() + "[ ]\\(%object%\\)",
				new PatternInfo(operator, true, true)});
			infos.add(new Object[] {"\\(%object%\\)[ ]" + operator.sign() + "[ ]%object%",
				new PatternInfo(operator, true, false)});
			infos.add(new Object[] {"%object%[ ]" + operator.sign() + "[ ]\\(%object%\\)",
				new PatternInfo(operator, false, true)});
			infos.add(new Object[] {"%object%[ ]" + operator.sign() + "[ ]%object%",
				new PatternInfo(operator, false, false)});
		}
		Object[][] arr = new Object[infos.size()][];
		for (int i = 0; i < arr.length; i++)
			arr[i] = infos.get(i);
		patterns = new Patterns<>(arr);
		//noinspection unchecked
		Skript.registerExpression(ExprArithmetic.class, Object.class,
			patterns.getPatterns());
	}

	private Expression<L> first;
	private Expression<R> second;
	private Operator operator;

	private Class<? extends T> returnType;
	private Collection<Class<?>> knownReturnTypes;

	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();

	// A parsed chain, like a tree
	private ArithmeticGettable<? extends T> arithmeticGettable;

	private boolean leftGrouped, rightGrouped, isTopLevel;

	@Override
	@SuppressWarnings({"ConstantConditions", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		first = (Expression<L>) exprs[0];
		second = (Expression<R>) exprs[1];

		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		leftGrouped = patternInfo.leftGrouped;
		rightGrouped = patternInfo.rightGrouped;
		operator = patternInfo.operator;

		// check if this is the top-level arithmetic expression (not part of a larger expression)
		ParsingStack stack = getParser().getParsingStack();
		isTopLevel = stack.isEmpty() || stack.peek().getSyntaxElementClass() != ExprArithmetic.class;

		// print warning for arg-1 confusion scenario
		printArgWarning(first, second, operator);

		if (first instanceof UnparsedLiteral) {
			if (second instanceof UnparsedLiteral) { // first and second need converting
				for (OperationInfo<?, ?, ?> operation : Arithmetics.getOperations(operator)) {
					// match left type with 'first'
					Expression<?> convertedFirst = first.getConvertedExpression(operation.left());
					if (convertedFirst == null)
						continue;
					// match right type with 'second'
					Expression<?> convertedSecond = second.getConvertedExpression(operation.right());
					if (convertedSecond == null)
						continue;
					// success, set the values
					first = (Expression<L>) convertedFirst;
					second = (Expression<R>) convertedSecond;
					returnType = (Class<? extends T>) operation.returnType();
				}
			} else { // first needs converting
				Class<?> secondClass = second.getReturnType();
				List<? extends OperationInfo<?, ?, ?>> operations = Arithmetics.lookupRightOperations(operator,
					secondClass);
				if (operations.isEmpty()) {
					if (secondClass != Object.class)
						return error(first.getReturnType(), secondClass);
					first = (Expression<L>) first.getConvertedExpression(Object.class);
				} else {
					first = (Expression<L>) first.getConvertedExpression(operations.stream()
							.map(OperationInfo::left)
							.toArray(Class[]::new));
				}
			}
		} else if (second instanceof UnparsedLiteral) { // second needs converting
			Class<?> firstClass = first.getReturnType();
			List<? extends OperationInfo<?, ?, ?>> operations = Arithmetics.lookupLeftOperations(operator, firstClass);
			if (operations.isEmpty()) {
				if (firstClass != Object.class)
					return error(firstClass, second.getReturnType());
				second = (Expression<R>) second.getConvertedExpression(Object.class);
			} else {
				second = (Expression<R>) second.getConvertedExpression(operations.stream()
						.map(OperationInfo::right)
						.toArray(Class[]::new));
			}
		}

		if (!LiteralUtils.canInitSafely(first) || !LiteralUtils.canInitSafely(second))
			return false;

		Class<? extends L> firstClass = first.getReturnType();
		Class<? extends R> secondClass = second.getReturnType();

		if (firstClass == Object.class || secondClass == Object.class) {
			Class<?>[] returnTypes = null;
			if (!(firstClass == Object.class && secondClass == Object.class)) {
				if (firstClass == Object.class) {
					returnTypes = Arithmetics.lookupRightOperations(operator, secondClass).stream()
							.map(OperationInfo::returnType)
							.toArray(Class[]::new);
				} else {
					returnTypes = Arithmetics.lookupLeftOperations(operator, firstClass).stream()
							.map(OperationInfo::returnType)
							.toArray(Class[]::new);
				}
			}
			if (returnTypes == null) {
				returnType = (Class<? extends T>) Object.class;
				knownReturnTypes = Arithmetics.getAllReturnTypes(operator);
			} else if (returnTypes.length == 0) {
				return error(firstClass, secondClass);
			} else {
				returnType = (Class<? extends T>) Classes.getSuperClassInfo(returnTypes[0]).getC();
				knownReturnTypes = Set.copyOf(List.of(returnTypes));
			}
		} else if (returnType == null) {
			OperationInfo<L, R, T> operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(
				operator, firstClass, secondClass);
			if (operationInfo == null)
				return error(firstClass, secondClass);
			returnType = operationInfo.returnType();
		}

		if (first instanceof ExprArithmetic && !leftGrouped) {
			chain.addAll(((ExprArithmetic<?, ?, L>) first).chain);
		} else {
			chain.add(first);
		}
		chain.add(operator);
		if (second instanceof ExprArithmetic && !rightGrouped) {
			chain.addAll(((ExprArithmetic<?, ?, R>) second).chain);
		} else {
			chain.add(second);
		}

		arithmeticGettable = ArithmeticChain.parse(chain);
		return arithmeticGettable != null || error(firstClass, secondClass);
	}

	private void printArgWarning(Expression<L> first, Expression<R> second, Operator operator) {
		if (operator == Operator.SUBTRACTION && !rightGrouped && !leftGrouped
			&& first instanceof ExprArgument argument && argument.couldCauseArithmeticConfusion()
			&& second instanceof ExprArithmetic<?, ?, ?> secondArith && secondArith.first instanceof Literal<?> literal
			&& literal.canReturn(Number.class)) {
			Literal<?> secondLiteral = (Literal<?>) LiteralUtils.defendExpression(literal);
			if (LiteralUtils.canInitSafely(secondLiteral)) {
				double number = ((Number) secondLiteral.getSingle(null)).doubleValue();
				if (number == 1)
					Skript.warning("This subtraction is ambiguous and could be interpreted as either the " +
						"'first argument' expression ('argument-1') or as subtraction from the argument " +
						"value ('(argument) - 1'). " +
					"If you meant to use 'argument-1', omit the hyphen ('arg 1') or use parentheses " +
						"to clarify your intent.");
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected T[] get(SkriptEvent event) {
		T result = arithmeticGettable.get(event);
		T[] one = (T[]) Array.newInstance(result == null ? returnType : result.getClass(), 1);
		one[0] = result;
		return one;
	}

	private boolean error(Class<?> firstClass, Class<?> secondClass) {
		// Errors are intentionally not logged here. During expression parsing, the parser
		// tries multiple operator splits (e.g. subtraction on "loop-iteration-2") and most
		// fail. Logging Skript.error() for each failure produces confusing noise even when
		// the expression eventually parses via a different operator. The statement-level
		// ParseLogHandler will report the appropriate error if nothing parses at all.
		return false;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public Class<? extends T>[] possibleReturnTypes() {
		if (returnType == Object.class)
			//noinspection unchecked
			return knownReturnTypes.toArray(new Class[0]);
		return super.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		if (this.returnType == Object.class && knownReturnTypes.contains(returnType))
			return true;
		return super.canReturn(returnType);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		String one = first.toString(event, debug);
		String two = second.toString(event, debug);
		if (leftGrouped)
			one = '(' + one + ')';
		if (rightGrouped)
			two = '(' + two + ')';
		return one + ' ' + operator + ' ' + two;
	}

	@Override
	public Expression<T> simplify() {
		if (isTopLevel)
			return simplifyInternal();
		return this;
	}

	private Expression<T> simplifyInternal() {
		if (first instanceof ExprArithmetic<?,?,?> firstArith) {
			//noinspection unchecked
			first = (Expression<L>) firstArith.simplifyInternal();
		} else {
			//noinspection unchecked
			first = (Expression<L>) first.simplify();
		}

		if (second instanceof ExprArithmetic<?,?,?> secondArith) {
			//noinspection unchecked
			second = (Expression<R>) secondArith.simplifyInternal();
		} else {
			//noinspection unchecked
			second = (Expression<R>) second.simplify();
		}

		if (first instanceof Literal && second instanceof Literal)
			return SimplifiedLiteral.fromExpression(this);

		return this;
	}

	/**
	 * For testing purposes only.
	 * @return the first expression
	 */
	Expression<L> getFirst() {
		return first;
	}

	/**
	 * For testing purposes only.
	 * @return the second expression
	 */
	Expression<R> getSecond() {
		return second;
	}

}
