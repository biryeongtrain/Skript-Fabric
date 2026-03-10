package ch.njol.skript.effects;

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

@Name("Handedness")
@Description("Make mobs left or right-handed. This does not affect players.")
@Example("""
    spawn skeleton at spawn of world "world":
        make entity left handed
    """)
@Example("make all zombies in radius 10 of player right handed")
@Since("2.8.0")
public class EffHandedness extends Effect {

    private Expression<LivingEntity> livingEntities;
    private boolean leftHanded;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(LivingEntity.class)) {
            return false;
        }
        livingEntities = (Expression<LivingEntity>) expressions[0];
        leftHanded = parseResult.hasTag("left");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity livingEntity : livingEntities.getAll(event)) {
            if (livingEntity instanceof Mob mob) {
                mob.setLeftHanded(leftHanded);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + livingEntities.toString(event, debug) + " " + (leftHanded ? "left" : "right") + " handed";
    }
}
