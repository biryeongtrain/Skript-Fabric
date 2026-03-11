package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.PrivateAllayAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Dance")
@Description({
        "Make an allay or piglin start or stop dancing.",
        "Providing a location only applies to allays.",
        "Providing a timespan only applies for piglins."
})
@Example("""
        if last spawned allay is not dancing:
            make last spawned allay start dancing
        """)
@Example("make last spawned piglin start dancing")
@Example("make all piglins dance for 5 hours")
@Since("2.11")
public class EffDancing extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private boolean start;
    private @Nullable Expression<FabricLocation> location;
    private @Nullable Expression<Timespan> timespan;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffDancing.class,
                "make %livingentities% (start dancing|dance) [%-location%] [timespan:for %-timespan%]",
                "make %livingentities% (stop dancing|not dance)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        start = matchedPattern == 0;
        if (start && exprs[1] != null) {
            location = (Expression<FabricLocation>) exprs[1];
        }
        if (parseResult.hasTag("timespan")) {
            timespan = (Expression<Timespan>) exprs[2];
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricLocation danceLocation = location == null ? null : location.getSingle(event);
        long danceTime = 0L;
        if (timespan != null) {
            Timespan span = timespan.getSingle(event);
            if (span != null) {
                danceTime = span.getAs(Timespan.TimePeriod.TICK);
            }
        }
        for (LivingEntity entity : entities.getArray(event)) {
            if (entity instanceof Allay allay) {
                allay.setDancing(start);
                if (start && danceLocation != null) {
                    PrivateAllayAccess.setJukeboxPos(allay, BlockPos.containing(danceLocation.position()));
                }
            } else if (entity instanceof Piglin piglin) {
                if (danceTime > 0L) {
                    EffectRuntimeSupport.invokeCompatible(piglin, "setDancing", true);
                } else {
                    piglin.setDancing(start);
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("make", entities, start ? "start dancing" : "stop dancing");
        if (location != null) {
            builder.append(location);
        }
        if (timespan != null) {
            builder.append("for", timespan);
        }
        return builder.toString();
    }
}
