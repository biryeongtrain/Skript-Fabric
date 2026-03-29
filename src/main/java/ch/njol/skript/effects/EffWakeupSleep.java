package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.fox.Fox;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Wake And Sleep")
@Description({
        "Make bats and foxes sleep or wake up.",
        "Make villagers or players sleep by providing a bed location."
})
@Example("make {_fox} go to sleep")
@Example("make player wake up without spawn location update")
@Since("2.11")
public class EffWakeupSleep extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffWakeupSleep.class,
                "make %livingentities% (start sleeping|[go to] sleep) [%-direction% %-location%]",
                "force %livingentities% to (start sleeping|[go to] sleep) [%-direction% %-location%]",
                "make %players% (start sleeping|[go to] sleep) %direction% %location% (force:with force)",
                "force %players% to (start sleeping|[go to] sleep) %direction% %location% (force:with force)",
                "make %livingentities% (stop sleeping|wake up)",
                "force %livingentities% to (stop sleeping|wake up)",
                "make %players% (stop sleeping|wake up) (spawn:without spawn [location] update)",
                "force %players% to (stop sleeping|wake up) (spawn:without spawn [location] update)"
        );
        registered = true;
    }

    private Expression<LivingEntity> entities;
    private @Nullable Expression<FabricLocation> locations;
    private boolean isSleep;
    private boolean force;
    private boolean noSpawnUpdate;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        isSleep = matchedPattern <= 3;
        force = matchedPattern == 1 || matchedPattern == 3 || parseResult.hasTag("force");
        noSpawnUpdate = parseResult.hasTag("spawn");

        if (matchedPattern <= 3 && exprs.length > 2 && exprs[1] != null && exprs[2] != null) {
            locations = Direction.combine(
                    (Expression<? extends Direction>) exprs[1],
                    (Expression<? extends FabricLocation>) exprs[2]
            );
        }

        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity entity : entities.getArray(event)) {
            if (isSleep) {
                executeSleep(event, entity);
            } else {
                executeWake(entity);
            }
        }
    }

    private void executeSleep(SkriptEvent event, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            if (locations != null) {
                FabricLocation loc = locations.getSingle(event);
                if (loc != null) {
                    BlockPos bedPos = BlockPos.containing(loc.position());
                    player.startSleepInBed(bedPos);
                }
            }
        } else if (entity instanceof Bat bat) {
            bat.setResting(true);
        } else if (entity instanceof Fox) {
            EffectRuntimeSupport.invokeCompatible(entity, "setSleeping", true);
        }
    }

    private void executeWake(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            player.stopSleepInBed(true, !noSpawnUpdate);
        } else if (entity instanceof Bat bat) {
            bat.setResting(false);
        } else if (entity instanceof Fox) {
            EffectRuntimeSupport.invokeCompatible(entity, "setSleeping", false);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String verb = isSleep ? " go to sleep" : " wake up";
        String result = "make " + entities.toString(event, debug) + verb;
        if (isSleep && locations != null) {
            result += " at " + locations.toString(event, debug);
        }
        if (force) {
            result += " with force";
        }
        if (noSpawnUpdate) {
            result += " without spawn location update";
        }
        return result;
    }
}
