package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class LitConsole extends SimpleExpression<MinecraftServer> {

    private static final String[] PATTERNS = {"[the] (console|server)"};

    public static void register() {
        Skript.registerExpression(LitConsole.class, MinecraftServer.class, PATTERNS);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected MinecraftServer @Nullable [] get(SkriptEvent event) {
        MinecraftServer server = resolveServer(event);
        return server == null ? new MinecraftServer[0] : new MinecraftServer[]{server};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends MinecraftServer> getReturnType() {
        return MinecraftServer.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the console";
    }

    private static @Nullable MinecraftServer resolveServer(@Nullable SkriptEvent event) {
        if (event == null) {
            return null;
        }
        if (event.server() != null) {
            return event.server();
        }
        if (event.player() != null && event.player().level().getServer() != null) {
            return event.player().level().getServer();
        }
        if (event.level() != null) {
            return event.level().getServer();
        }
        return null;
    }
}
