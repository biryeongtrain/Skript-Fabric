package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricServerListPingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.List;

@Name("Hover List")
@Description({
	"The list when you hover on the player counts of the server in the server list.",
	"This can be changed using texts or players in a server list ping event only."
})
@Example("""
	on server list ping:
		clear the hover list
		add "&aWelcome to the server!" to the hover list
	""")
@Since("2.3, Fabric")
@Events("server list ping")
public class ExprHoverList extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprHoverList.class, String.class,
			"[the] [custom] [player|server] (hover|sample) ([message] list|message)",
			"[the] [custom] player [hover|sample] list");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(FabricServerListPingEventHandle.class)) {
			Skript.error("The hover list expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	public String @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricServerListPingEventHandle handle)) {
			return null;
		}
		return handle.playerSample().toArray(new String[0]);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return new Class[]{String[].class, ServerPlayer[].class};
		}
		return null;
	}

	@Override
	public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event.handle() instanceof FabricServerListPingEventHandle handle)) {
			return;
		}

		List<String> sample = handle.playerSample();
		switch (mode) {
			case SET:
				sample.clear();
				// fall through
			case ADD:
				if (delta != null) {
					for (Object object : delta) {
						if (object instanceof ServerPlayer player) {
							sample.add(player.getGameProfile().getName());
						} else {
							sample.add((String) object);
						}
					}
				}
				break;
			case REMOVE:
				if (delta != null) {
					for (Object value : delta) {
						String name = value instanceof ServerPlayer player
								? player.getGameProfile().getName()
								: (String) value;
						sample.remove(name);
					}
				}
				break;
			case DELETE:
			case RESET:
				sample.clear();
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "the hover list";
	}
}
