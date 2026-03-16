package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricChatEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.Set;

@Name("Chat Recipients")
@Description("Recipients of chat events where this is called.")
@Example("chat recipients")
@Since("2.2-Fixes-v7, 2.2-dev35 (clearing recipients), Fabric")
public class ExprChatRecipients extends SimpleExpression<ServerPlayer> {

	static {
		Skript.registerExpression(ExprChatRecipients.class, ServerPlayer.class, "[the] [chat][( |-)]recipients");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(FabricChatEventHandle.class)) {
			Skript.error("Cannot use chat recipients expression outside of a chat event");
			return false;
		}
		return true;
	}

	@Override
	protected ServerPlayer @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricChatEventHandle handle)) {
			return null;
		}
		Set<ServerPlayer> playerSet = handle.recipients();
		return playerSet.toArray(new ServerPlayer[0]);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return new Class[]{ServerPlayer[].class};
	}

	@Override
	public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event.handle() instanceof FabricChatEventHandle handle)) {
			return;
		}
		ServerPlayer[] recipients = delta != null ? (ServerPlayer[]) delta : null;
		switch (mode) {
			case REMOVE:
				if (recipients != null) {
					for (ServerPlayer player : recipients) {
						handle.recipients().remove(player);
					}
				}
				break;
			case ADD:
				if (recipients != null) {
					for (ServerPlayer player : recipients) {
						handle.recipients().add(player);
					}
				}
				break;
			case SET:
				handle.recipients().clear();
				if (recipients != null) {
					for (ServerPlayer player : recipients) {
						handle.recipients().add(player);
					}
				}
				break;
			case RESET:
			case DELETE:
				handle.recipients().clear();
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<ServerPlayer> getReturnType() {
		return ServerPlayer.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "chat recipients";
	}
}
