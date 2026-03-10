package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Swing Hand")
@Description("Makes an entity swing their hand. This does nothing if the entity does not have an animation for swinging their hand.")
@Example("make player swing their main hand")
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.15.2+")
public class EffSwingHand extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean isMainHand;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffSwingHand.class,
                "make %livingentities% swing [their] [main] hand",
                "make %livingentities% swing [their] off[ ]hand"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        isMainHand = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        for (LivingEntity entity : entities.getArray(event)) {
            entity.swing(hand);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + entities.toString(event, debug) + " swing their " + (isMainHand ? "hand" : "off hand");
    }
}
