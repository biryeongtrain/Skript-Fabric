package ch.njol.skript.command;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single argument in a script command definition.
 * Parsed from syntax like {@code <name:type>} or {@code [<name:type=default>]}.
 */
public final class Argument {

	private final String name;
	private final ClassInfo<?> classInfo;
	private final boolean optional;
	private final @Nullable String defaultExpression;
	private final int index;

	public Argument(String name, ClassInfo<?> classInfo, boolean optional, @Nullable String defaultExpression, int index) {
		this.name = name;
		this.classInfo = classInfo;
		this.optional = optional;
		this.defaultExpression = defaultExpression;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public ClassInfo<?> getClassInfo() {
		return classInfo;
	}

	public boolean isOptional() {
		return optional;
	}

	public @Nullable String getDefaultExpression() {
		return defaultExpression;
	}

	public int getIndex() {
		return index;
	}

	/**
	 * Parses a single token using this argument's ClassInfo parser.
	 *
	 * @param input the raw input token
	 * @return the parsed value, or null if parsing failed
	 */
	public @Nullable Object parse(String input) {
		if (input == null || input.isEmpty()) {
			return parseDefault();
		}
		return Classes.parse(input, classInfo.getC(), ParseContext.COMMAND);
	}

	/**
	 * Parses the default value, if one exists.
	 */
	public @Nullable Object parseDefault() {
		if (defaultExpression == null || defaultExpression.isEmpty()) {
			return null;
		}
		return Classes.parse(defaultExpression, classInfo.getC(), ParseContext.COMMAND);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (optional) sb.append('[');
		sb.append('<').append(name).append(':').append(classInfo.getCodeName());
		if (defaultExpression != null) {
			sb.append('=').append(defaultExpression);
		}
		sb.append('>');
		if (optional) sb.append(']');
		return sb.toString();
	}
}
