package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.events.FabricPlayerEventHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Spectator Target")
@Description("Grabs the spectator target entity of the players.")
@Example("""
	on player start spectating of player:
		message "&c%spectator target% currently has %{game::kills::%spectator target%}% kills!" to the player
	""")
@Example("""
	on player stop spectating:
		set spectator target to the nearest skeleton
	""")
@Since("2.4-alpha4, 2.7 (Spectator Event)")
public class ExprSpectatorTarget extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprSpectatorTarget.class, Entity.class,
				"spectator target [of %-players%]",
				"%players%'[s] spectator target"
		);
	}

	private Expression<ServerPlayer> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<ServerPlayer>) expressions[0];
		if (players == null) {
			if (!getParser().isCurrentEvent(FabricPlayerEventHandles.Spectate.class)) {
				Skript.error("The expression 'spectator target' may only be used in a start/stop/swap spectating target event");
				return false;
			}
		}
		return true;
	}

	@Override
	@Nullable
	protected Entity[] get(SkriptEvent event) {
		if (players == null) {
			if (event.handle() instanceof FabricPlayerEventHandles.Spectate handle) {
				return switch (handle.action()) {
					case START, SWAP -> {
						Entity target = handle.newTarget();
						yield target != null ? new Entity[]{target} : new Entity[0];
					}
					case STOP -> {
						Entity target = handle.currentTarget();
						yield target != null ? new Entity[]{target} : new Entity[0];
					}
				};
			}
			return new Entity[0];
		}
		return players.stream(event)
				.map(player -> {
					Entity camera = player.getCamera();
					return camera != player ? camera : null;
				})
				.filter(e -> e != null)
				.toArray(Entity[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE)
			return new Class[]{Entity.class};
		return null;
	}

	@Override
	public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
		if (players == null)
			return;
		switch (mode) {
			case SET:
				assert delta != null;
				for (ServerPlayer player : players.getArray(event)) {
					if (player.isSpectator())
						player.setCamera((Entity) delta[0]);
				}
				break;
			case RESET:
			case DELETE:
				for (ServerPlayer player : players.getArray(event)) {
					if (player.isSpectator())
						player.setCamera(player);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return players == null || players.isSingle();
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "spectator target" + (players != null ? " of " + players.toString(event, debug) : "");
	}

}
