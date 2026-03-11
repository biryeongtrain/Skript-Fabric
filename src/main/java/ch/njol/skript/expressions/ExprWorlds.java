package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorlds extends SimpleExpression<ServerLevel> {

    static {
        Skript.registerExpression(ExprWorlds.class, ServerLevel.class, "[(all [[of] the]|the)] worlds");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected ServerLevel @Nullable [] get(SkriptEvent event) {
        MinecraftServer server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null) {
            return new ServerLevel[0];
        }
        List<ServerLevel> worlds = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            worlds.add(level);
        }
        return worlds.toArray(ServerLevel[]::new);
    }

    @Override
    public @Nullable Iterator<ServerLevel> iterator(SkriptEvent event) {
        ServerLevel[] worlds = get(event);
        return worlds == null ? null : java.util.Arrays.asList(worlds).iterator();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends ServerLevel> getReturnType() {
        return ServerLevel.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "worlds";
    }
}
