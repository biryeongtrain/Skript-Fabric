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
import net.minecraft.world.entity.animal.allay.Allay;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateAllayAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Allay Duplicate")
@Description({
        "Set whether an allay can or cannot duplicate itself.",
        "This is not the same as breeding allays."
})
@Example("allow all allays to duplicate")
@Example("prevent all allays from duplicating")
@Since("2.11")
public class EffAllayCanDuplicate extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean duplicate;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffAllayCanDuplicate.class,
                "allow %livingentities% to (duplicate|clone)",
                "prevent %livingentities% from (duplicating|cloning)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        duplicate = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getArray(event)) {
            if (entity instanceof Allay allay) {
                PrivateAllayAccess.setCanDuplicate(allay, duplicate);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return duplicate
                ? "allow " + entities.toString(event, debug) + " to duplicate"
                : "prevent " + entities.toString(event, debug) + " from duplicating";
    }
}
