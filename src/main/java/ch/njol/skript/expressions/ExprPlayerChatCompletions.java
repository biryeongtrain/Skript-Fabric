package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Player Chat Completions")
@Description({
	"The custom chat completion suggestions.",
	"This expression will not return anything due to limitations."
})
@Since("2.10")
public class ExprPlayerChatCompletions extends SimplePropertyExpression<ServerPlayer, String> {

	static {
		register(ExprPlayerChatCompletions.class, String.class, "[custom] chat completion[s]", "players");
	}

	@Override
	public @Nullable String convert(ServerPlayer player) {
		return null;
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET -> new Class[]{String[].class};
			default -> null;
		};
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		ServerPlayer[] players = getExpr().getArray(event);
		if (players.length == 0) return;

		List<String> completions = new ArrayList<>();
		if (delta != null && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)) {
			completions = Arrays.stream(delta)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.collect(Collectors.toList());
		}

		switch (mode) {
			case DELETE, RESET -> {
				for (ServerPlayer player : players)
					ChatCompletionSupport.clear(player);
			}
			case SET -> {
				for (ServerPlayer player : players)
					ChatCompletionSupport.set(player, completions);
			}
			case ADD -> {
				for (ServerPlayer player : players)
					ChatCompletionSupport.add(player, completions);
			}
			case REMOVE -> {
				for (ServerPlayer player : players)
					ChatCompletionSupport.remove(player, completions);
			}
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "custom chat completions";
	}
}
