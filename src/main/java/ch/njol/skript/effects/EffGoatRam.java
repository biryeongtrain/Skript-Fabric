package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Goat Ram")
@Description({
        "Make a goat ram an entity.",
        "Ramming does have a cooldown and currently no way to change it."
})
@Example("make all goats ram player")
@Since("2.11")
public class EffGoatRam extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private Expression<LivingEntity> target;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffGoatRam.class,
                "make %livingentities% ram %livingentity%",
                "force %livingentities% to ram %livingentity%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        target = (Expression<LivingEntity>) exprs[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        LivingEntity ramTarget = target.getSingle(event);
        if (ramTarget == null) {
            return;
        }
        for (LivingEntity entity : entities.getArray(event)) {
            if (entity instanceof Goat goat) {
                Object invoked = EffectRuntimeSupport.invokeCompatible(goat, "ram", ramTarget);
                if (invoked == null) {
                    goat.getNavigation().moveTo(ramTarget, 1.25D);
                    goat.setTarget(ramTarget);
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + entities.toString(event, debug) + " ram " + target.toString(event, debug);
    }
}
