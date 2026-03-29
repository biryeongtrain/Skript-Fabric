package ch.njol.skript.expressions;

import java.util.Arrays;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Tablisted Players")
@Description({
	"The players shown in the tab lists of the specified players.",
	"`delete` will remove all the online players from the tab list.",
	"`reset` will reset the tab list to the default state, which makes all players visible again."
})
@Example("tablist players of player")
@Since("2.13")
@Keywords("tablist")
public class ExprTablistedPlayers extends PropertyExpression<ServerPlayer, ServerPlayer> {

	static {
		registerDefault(ExprTablistedPlayers.class, ServerPlayer.class, "(tablist[ed]|listed) players", "players");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ServerPlayer>) expressions[0]);
		return true;
	}

	@Override
	protected ServerPlayer[] get(SkriptEvent event, ServerPlayer[] source) {
		return Arrays.stream(source)
				.filter(viewer -> viewer.level().getServer() != null)
				.flatMap(viewer -> viewer.level().getServer().getPlayerList().getPlayers().stream()
						.filter(p -> !HiddenPlayerSupport.isHidden(viewer, p.getUUID()))
						.filter(p -> TabListedPlayerSupport.isListed(viewer, p.getUUID())))
				.distinct()
				.toArray(ServerPlayer[]::new);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return new Class[]{ServerPlayer[].class};
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		ServerPlayer[] recipients = (ServerPlayer[]) delta;
		ServerPlayer[] viewers = getExpr().getArray(event);
		switch (mode) {
			case DELETE:
				for (ServerPlayer viewer : viewers) {
					if (viewer.level().getServer() != null)
						for (ServerPlayer player : viewer.level().getServer().getPlayerList().getPlayers()) {
							TabListedPlayerSupport.unlist(viewer, player);
						}
				}
				break;
			case REMOVE:
				for (ServerPlayer viewer : viewers) {
					for (ServerPlayer player : recipients) {
						TabListedPlayerSupport.unlist(viewer, player);
					}
				}
				break;
			case SET:
				for (ServerPlayer viewer : viewers) {
					if (viewer.level().getServer() != null)
						for (ServerPlayer player : viewer.level().getServer().getPlayerList().getPlayers()) {
							if (Arrays.stream(recipients).noneMatch(recipient -> recipient.equals(player))) {
								TabListedPlayerSupport.unlist(viewer, player);
							}
						}
				}
				// fall through to ADD
			case ADD:
				assert recipients != null;
				for (ServerPlayer viewer : viewers) {
					for (ServerPlayer player : recipients) {
						if (!HiddenPlayerSupport.isHidden(viewer, player.getUUID())) {
							TabListedPlayerSupport.list(viewer, player);
						}
					}
				}
				break;
			case RESET:
				for (ServerPlayer viewer : viewers) {
					TabListedPlayerSupport.resetAll(viewer);
					if (viewer.level().getServer() != null)
						for (ServerPlayer player : viewer.level().getServer().getPlayerList().getPlayers()) {
							if (!HiddenPlayerSupport.isHidden(viewer, player.getUUID())) {
								TabListedPlayerSupport.list(viewer, player);
							}
						}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ServerPlayer> getReturnType() {
		return ServerPlayer.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "tablisted players of " + getExpr().toString(event, debug);
	}

}
