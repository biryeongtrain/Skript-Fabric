package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffInvisible extends Effect {

    private static boolean registered;

    private boolean invisible;
    private Expression<?> livingEntities;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffInvisible.class,
                "make %livingentities% not visible",
                "make %livingentities% not invisible",
                "make %livingentities% invisible",
                "make %livingentities% visible"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        livingEntities = expressions[0];
        invisible = matchedPattern == 0 || matchedPattern == 2;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Object rawEntity : livingEntities.getAll(event)) {
            if (rawEntity instanceof LivingEntity livingEntity) {
                livingEntity.setInvisible(invisible);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + livingEntities.toString(event, debug) + " " + (invisible ? "invisible" : "visible");
    }
}
