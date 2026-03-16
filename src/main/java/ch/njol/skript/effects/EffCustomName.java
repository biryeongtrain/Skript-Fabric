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
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Toggle Custom Name Visibility")
@Description("Toggles the custom name visibility of an entity.")
@Example("show the custom name of event-entity")
@Example("hide target's display name")
@Since("2.10")
public class EffCustomName extends Effect {

    static {
        Skript.registerEffect(
                EffCustomName.class,
                "(:show|hide) [the] (custom|display)[ ]name of %entities%",
                "(:show|hide) %entities%'[s] (custom|display)[ ]name"
        );
    }

    private Expression<Entity> entities;
    private boolean showCustomName;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        showCustomName = parseResult.hasTag("show");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Entity entity : entities.getAll(event)) {
            entity.setCustomNameVisible(showCustomName);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (showCustomName ? "show" : "hide") + " the custom name of " + entities.toString(event, debug);
    }
}
