package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Hidden Players")
@Description("The players hidden from a player that were hidden using the entity visibility effect.")
@Since("2.3")
public class ExprHiddenPlayers extends SimpleExpression<ServerPlayer> {

	static {
		Skript.registerExpression(ExprHiddenPlayers.class, ServerPlayer.class,
			"[(all [[of] the]|the)] hidden players (of|for) %players%",
			"[(all [[of] the]|the)] players hidden (from|for|by) %players%");
	}

	private Expression<ServerPlayer> viewers;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		viewers = (Expression<ServerPlayer>) exprs[0];
		return true;
	}

	@Override
	protected ServerPlayer @Nullable [] get(SkriptEvent event) {
		List<ServerPlayer> list = new ArrayList<>();
		for (ServerPlayer player : viewers.getArray(event)) {
			Set<UUID> hidden = HiddenPlayerSupport.getHidden(player);
			if (player.level().getServer() != null) {
				for (UUID uuid : hidden) {
					ServerPlayer target = player.level().getServer().getPlayerList().getPlayer(uuid);
					if (target != null) {
						list.add(target);
					}
				}
			}
		}
		return list.toArray(new ServerPlayer[0]);
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
		return "hidden players for " + viewers.toString(event, debug);
	}
}
