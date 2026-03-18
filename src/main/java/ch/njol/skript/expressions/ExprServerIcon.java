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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricServerListPingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Name("Server Icon")
@Description({
	"Icon of the server in the server list.",
	"'default server icon' returns the path to server-icon.png.",
	"'shown server icon' returns the currently set icon path in a server list ping event.",
	"Can be set to a file path (string) in a server list ping event."
})
@Example("""
	on script load:
		set {server-icon} to the default server icon
	""")
@Example("""
	on server list ping:
		set the icon to the last loaded server icon
	""")
@Since("2.3, Fabric")
public class ExprServerIcon extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprServerIcon.class, String.class,
				"[the] [(1:(default)|2:(shown|sent))] [server] icon");
	}

	private boolean isServerPingEvent, isDefault;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isServerPingEvent = getParser().isCurrentEvent(FabricServerListPingEventHandle.class);
		isDefault = (parseResult.mark == 0 && !isServerPingEvent) || parseResult.mark == 1;
		if (!isServerPingEvent && !isDefault) {
			Skript.error("The 'shown' server icon expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	protected String @Nullable [] get(SkriptEvent event) {
		if (!isDefault && event.handle() instanceof FabricServerListPingEventHandle handle) {
			byte[] bytes = handle.faviconBytes();
			if (bytes != null) {
				return new String[]{"custom-server-icon"};
			}
		}
		Path iconPath = Path.of("server-icon.png");
		if (Files.exists(iconPath)) {
			return new String[]{iconPath.toAbsolutePath().toString()};
		}
		return null;
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (isDefault) {
			return null;
		}
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
			return new Class[]{String.class};
		}
		return null;
	}

	@Override
	public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event.handle() instanceof FabricServerListPingEventHandle handle)) {
			return;
		}
		if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
			handle.setFaviconBytes(null);
			return;
		}
		if (delta == null || delta.length == 0) {
			return;
		}
		String filePath = (String) delta[0];
		// Check if it matches the last loaded icon path
		if (filePath.equals(ExprLastLoadedServerIcon.lastLoaded)) {
			byte[] bytes = ExprLastLoadedServerIcon.lastLoadedBytes;
			if (bytes != null) {
				handle.setFaviconBytes(bytes);
				return;
			}
		}
		// Otherwise load from file
		try {
			byte[] bytes = Files.readAllBytes(Path.of(filePath));
			handle.setFaviconBytes(bytes);
		} catch (IOException e) {
			Skript.error("Could not load server icon from file '" + filePath + "': " + e.getMessage());
		}
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
		return "the " + (!isServerPingEvent || isDefault ? "default" : "shown") + " server icon";
	}
}
