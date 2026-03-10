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
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Entity Despawn")
@Description({
        "Make a living entity despawn when the chunk they're located at is unloaded.",
        "Setting a custom name on a living entity automatically makes it not despawnable.",
        "More information on what and when entities despawn can be found at "
                + "<a href=\"https://minecraft.wiki/w/Mob_spawning#Despawning\">reference</a>."
})
@Example("make all entities not despawnable on chunk unload")
@Example("""
        spawn zombie at location(0, 0, 0):
            force event-entity to not despawn when far away
        """)
@Since("2.11")
public final class EffEntityUnload extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean despawn;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffEntityUnload.class,
                "make %livingentities% despawn[able] (on chunk unload|when far away)",
                "force %livingentities% to despawn (on chunk unload|when far away)",
                "prevent %livingentities% from despawning [on chunk unload|when far away]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        despawn = matchedPattern != 2;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getArray(event)) {
            if (despawn) {
                if (entity instanceof Mob mob) {
                    EffectRuntimeSupport.setBooleanField(mob, "persistenceRequired", false);
                } else {
                    EffectRuntimeSupport.setBooleanField(entity, "persistenceRequired", false);
                }
                continue;
            }
            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
            } else {
                EffectRuntimeSupport.setBooleanField(entity, "persistenceRequired", true);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (despawn) {
            return "make " + entities.toString(event, debug) + " despawn on chunk unload";
        }
        return "prevent " + entities.toString(event, debug) + " from despawning on chunk unload";
    }
}
