package org.skriptlang.skript.log.runtime;

import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.SyntaxElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A more versatile set of information about the source of an error.
 *
 * @param syntaxType A string representing the type of syntax.
 * @param syntaxName The name of the syntax emitting the error.
 * @param lineNumber The line number of the syntax emitting the error.
 * @param lineText The raw code of the line.
 * @param script The name of the script in which the line exists.
 */
public record ErrorSource(
	String syntaxType,
	String syntaxName,
	int lineNumber,
	String lineText,
	String script
) {

	public static @NotNull ErrorSource fromNodeAndElement(@Nullable Node node, @NotNull SyntaxElement element) {
		Name annotation = element.getClass().getAnnotation(Name.class);
		String elementName = annotation != null ? annotation.value().trim().replaceAll("\n", "") : element.getClass().getSimpleName();
		if (node == null) {
			return new ErrorSource(element.getSyntaxTypeName(), elementName, 0, "-unknown-", "-unknown-");
		}
		String code = node.getKey() != null ? node.getKey().trim() : "-unknown-";
		String fileName = node.getConfig() != null ? node.getConfig().getFileName() : "-unknown-";
		return new ErrorSource(element.getSyntaxTypeName(), elementName, node.getLine(), code, fileName);
	}

	@Contract(" -> new")
	public @NotNull Location location() {
		return new Location(script, lineNumber);
	}

	public record Location(String script, int lineNumber) { }

}
