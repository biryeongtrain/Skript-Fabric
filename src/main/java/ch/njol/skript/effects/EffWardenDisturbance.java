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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Disturbance")
@Description({
        "Make a warden sense a disturbance at a location, causing the warden to investigate that area.",
        "The warden will not investigate if the warden is aggressive towards an entity.",
        "This effect does not add anger to the warden."
})
@Example("make last spawned warden sense a disturbance at location(0, 0, 0)")
@Since("2.11")
public final class EffWardenDisturbance extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> wardens;
    private Expression<FabricLocation> location;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffWardenDisturbance.class,
                "make %livingentities% sense [a] disturbance at %location%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        wardens = (Expression<LivingEntity>) exprs[0];
        location = (Expression<FabricLocation>) exprs[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricLocation target = location.getSingle(event);
        if (target == null) {
            return;
        }
        BlockPos pos = BlockPos.containing(target.position());
        for (LivingEntity livingEntity : wardens.getArray(event)) {
            if (livingEntity instanceof Warden warden) {
                WardenAi.setDisturbanceLocation(warden, pos);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + wardens.toString(event, debug) + " sense a disturbance at " + location.toString(event, debug);
    }
}
