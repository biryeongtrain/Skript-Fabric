package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandContext;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Expressions for reading cooldown information of the current script command.
 *
 * <ul>
 *   <li>remaining cooldown — time left for the executing player</li>
 *   <li>cooldown — the total cooldown duration</li>
 *   <li>cooldown bypass permission — the bypass permission string</li>
 * </ul>
 */
public class ExprCmdCooldownInfo extends SimpleExpression<Object> {

	private enum CooldownInfoType {
		REMAINING, TOTAL, BYPASS
	}

	static {
		Skript.registerExpression(ExprCmdCooldownInfo.class, Object.class,
				"[the] remaining cooldown",
				"[the] cooldown [time|duration]",
				"[the] cooldown bypass [permission]",
				"[the] elapsed cooldown [time]",
				"[the] last usage [date|time]");
	}

	private CooldownInfoType type = CooldownInfoType.REMAINING;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ScriptCommandContext.class)) {
			Skript.error("Cooldown info expressions can only be used in a command trigger.");
			return false;
		}
		type = CooldownInfoType.values()[matchedPattern];
		return true;
	}

	@Override
	protected Object @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof ScriptCommandContext ctx)) {
			return new Object[0];
		}
		ScriptCommand cmd = ctx.command();
		switch (type) {
			case REMAINING -> {
				ServerPlayer player = ctx.source().getPlayer();
				if (player == null || cmd.getCooldownMillis() <= 0) {
					return new Object[]{0L};
				}
				Map<UUID, Long> cooldowns = Commands.getCooldowns(cmd.getName());
				Long lastUsed = cooldowns.get(player.getUUID());
				if (lastUsed == null) {
					return new Object[]{0L};
				}
				long remaining = cmd.getCooldownMillis() - (System.currentTimeMillis() - lastUsed);
				return new Object[]{Math.max(0L, remaining)};
			}
			case TOTAL -> {
				return new Object[]{cmd.getCooldownMillis()};
			}
			case BYPASS -> {
				String bypass = cmd.getCooldownBypass();
				return bypass != null ? new String[]{bypass} : new String[0];
			}
		}
		return new Object[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return type == CooldownInfoType.BYPASS ? String.class : Long.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return switch (type) {
			case REMAINING -> "the remaining cooldown";
			case TOTAL -> "the cooldown";
			case BYPASS -> "the cooldown bypass permission";
		};
	}
}
