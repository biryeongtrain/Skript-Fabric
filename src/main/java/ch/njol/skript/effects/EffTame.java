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
import net.minecraft.world.entity.TamableAnimal;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Tame / Untame")
@Description("Tame a tameable entity (wolf, parrot, cat, etc.).")
@Example("tame {_wolf}")
@Example("untame {_wolf}")
@Since("2.10")
public class EffTame extends Effect {

    static {
        Skript.registerEffect(EffTame.class, "[:un](tame|domesticate) %entities%");
    }

    private Expression<Entity> entities;
    private boolean tame;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        tame = !parseResult.hasTag("un");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Entity entity : entities.getAll(event)) {
            if (entity instanceof TamableAnimal tamableAnimal) {
                tamableAnimal.setTame(tame, true);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (tame ? "tame " : "untame ") + entities.toString(event, debug);
    }
}
