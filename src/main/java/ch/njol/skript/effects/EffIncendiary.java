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
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricExplosionPrimeEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Incendiary")
@Description("Sets if an entity's explosion will leave behind fire. This effect is also usable in an explosion prime event.")
@Example("""
        on explosion prime:
            make the explosion fiery
        """)
@Since("2.5")
public class EffIncendiary extends Effect {

    private static boolean registered;

    private Expression<Entity> entities;
    private boolean causeFire;
    private boolean isEvent;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffIncendiary.class,
                "make %entities% [(1¦not)] incendiary",
                "make %entities%'[s] explosion [(1¦not)] (incendiary|fiery)",
                "make [the] [event(-| )]explosion [(1¦not)] (incendiary|fiery)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        isEvent = matchedPattern == 2;
        if (isEvent && !getParser().isCurrentEvent(FabricEffectEventHandles.ExplosionPrime.class)) {
            Skript.error("Making 'the explosion' fiery is only usable in an explosion prime event");
            return false;
        }
        if (!isEvent) {
            entities = (Expression<Entity>) exprs[0];
        }
        causeFire = parseResult.mark != 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (isEvent) {
            if (event.handle() instanceof FabricExplosionPrimeEventHandle explosion) {
                explosion.setCausesFire(causeFire);
            }
            return;
        }
        for (Entity entity : entities.getArray(event)) {
            EffectRuntimeSupport.invokeCompatible(entity, new String[]{"setIsIncendiary", "setIncendiary"}, causeFire);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (isEvent) {
            return "make the event-explosion " + (causeFire ? "" : "not ") + "fiery";
        }
        return "make " + entities.toString(event, debug) + (causeFire ? "" : " not") + " incendiary";
    }
}
