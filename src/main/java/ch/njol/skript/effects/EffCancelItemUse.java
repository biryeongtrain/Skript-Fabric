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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Cancel Active Item")
@Description({
        "Interrupts the action entities may be trying to complete.",
        "For example, interrupting eating, or drawing back a bow."
})
@Example("""
        on damage of player:
            if the victim's active tool is a bow:
                interrupt the usage of the player's active item
        """)
@Since("2.8.0")
public final class EffCancelItemUse extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffCancelItemUse.class,
                "(cancel|interrupt) [the] us[ag]e of %livingentities%'[s] [active|current] item"
        );
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
            entity.stopUsingItem();
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "cancel the usage of " + entities.toString(event, debug) + "'s active item";
    }
}
