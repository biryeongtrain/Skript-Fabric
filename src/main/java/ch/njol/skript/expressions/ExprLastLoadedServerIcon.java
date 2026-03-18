package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Last Loaded Server Icon")
@Description("Returns the last loaded server icon. Currently a stub on Fabric.")
@Example("set {server-icon} to the last loaded server icon")
@Since("2.3, Fabric")
public class ExprLastLoadedServerIcon extends SimpleExpression<String> {

	/**
	 * Static field storing the file path of the last loaded server icon.
	 */
	public static volatile @Nullable String lastLoaded = null;

	/**
	 * Static field storing the raw PNG bytes of the last loaded server icon.
	 */
	public static volatile byte @Nullable [] lastLoadedBytes = null;

	static {
		Skript.registerExpression(ExprLastLoadedServerIcon.class, String.class, "[the] [last[ly]] loaded server icon");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected String @Nullable [] get(SkriptEvent event) {
		String icon = lastLoaded;
		if (icon == null) {
			return null;
		}
		return new String[]{icon};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "the last loaded server icon";
	}
}
