package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class WorldInitializationGameTestBootstrap implements ModInitializer {

    private static final Map<ResourceKey<Level>, Integer> INITIALIZATION_COUNTS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        Skript.registerEffect(RecordWorldInitializationEffect.class, "record gametest world initialization");
        SkriptRuntime.instance().loadFromResource("skript/gametest/event/world_initialization_records_runtime.sk");
    }

    public static int initializationCount(ResourceKey<Level> worldKey) {
        return INITIALIZATION_COUNTS.getOrDefault(worldKey, 0);
    }

    public static final class RecordWorldInitializationEffect extends Effect {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (event.level() != null) {
                INITIALIZATION_COUNTS.merge(event.level().dimension(), 1, Integer::sum);
            }
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "record gametest world initialization";
        }
    }
}
