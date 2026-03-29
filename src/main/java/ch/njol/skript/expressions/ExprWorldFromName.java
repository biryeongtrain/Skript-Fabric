package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorldFromName extends SimpleExpression<ServerLevel> {

    static {
        Skript.registerExpression(ExprWorldFromName.class, ServerLevel.class, "[the] world [(named|with name)] %string%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<String> worldName;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        worldName = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected ServerLevel @Nullable [] get(SkriptEvent event) {
        String input = worldName.getSingle(event);
        MinecraftServer server = ExpressionRuntimeSupport.resolveServer(event);
        if (input == null || input.isBlank() || server == null) {
            return null;
        }
        String normalized = input.trim();
        for (ServerLevel world : server.getAllLevels()) {
            if (matches(world.dimension(), normalized)) {
                return new ServerLevel[]{world};
            }
        }
        return null;
    }

    private static boolean matches(ResourceKey<Level> key, String input) {
        if (input.equalsIgnoreCase(String.valueOf(key))) {
            return true;
        }
        Identifier location = key.identifier();
        return input.equalsIgnoreCase(location.toString())
                || input.equalsIgnoreCase(location.getPath());
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<ServerLevel> getReturnType() {
        return ServerLevel.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the world with name " + worldName.toString(event, debug);
    }
}
