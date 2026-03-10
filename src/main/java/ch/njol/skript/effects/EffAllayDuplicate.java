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
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Allay Duplicate")
@Description({
        "Make an allay duplicate itself.",
        "This effect will always make an allay duplicate regardless of whether the duplicate attribute is disabled."
})
@Example("make all allays duplicate")
@Since("2.11")
public class EffAllayDuplicate extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffAllayDuplicate.class, "make %livingentities% (duplicate|clone)");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getArray(event)) {
            if (entity instanceof Allay allay) {
                EffectRuntimeSupport.invokeCompatible(allay, new String[]{"duplicateAllay", "duplicate"});
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + entities.toString(event, debug) + " duplicate";
    }
}
