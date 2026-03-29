package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.panda.Panda;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffPandaOnBack extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean getOn;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffPandaOnBack.class,
                "make %livingentities% get (:on|off) (its|their) back[s]",
                "force %livingentities% to get (:on|off) (its|their) back[s]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(LivingEntity.class)) {
            return false;
        }
        entities = (Expression<LivingEntity>) expressions[0];
        getOn = parseResult.hasTag("on");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getAll(event)) {
            if (entity instanceof Panda panda) {
                panda.setOnBack(getOn);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + entities.toString(event, debug) + " get " + (getOn ? "on" : "off") + " their backs";
    }
}
