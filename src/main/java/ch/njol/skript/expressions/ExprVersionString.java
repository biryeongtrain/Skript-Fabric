package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVersionString extends SimpleExpression<String> {

	static {
		Skript.registerExpression(
				ExprVersionString.class,
				String.class,
				"[the] [server] version [string|text]"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected String[] get(SkriptEvent event) {
		return new String[]{SharedConstants.getCurrentVersion().name()};
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
		return "the server version string";
	}

}
