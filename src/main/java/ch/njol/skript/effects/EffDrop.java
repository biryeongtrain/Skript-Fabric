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
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Drop")
@Description("Drops one or more items.")
@Example("""
        on death of creeper:
            drop 1 TNT
        """)
@Since("1.0")
public class EffDrop extends Effect {

    private static boolean registered;

    @Nullable
    public static Entity lastSpawned = null;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffDrop.class, "drop %itemtypes/experiences% [%directions% %locations%] [(1¦without velocity)]");
        registered = true;
    }

    private Expression<?> drops;
    private Expression<FabricLocation> locations;
    private boolean noVelocity;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        drops = exprs[0];
        locations = Direction.combine(
                (Expression<? extends Direction>) exprs[1],
                (Expression<? extends FabricLocation>) exprs[2]
        );
        noVelocity = parseResult.mark == 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricLocation[] locs = locations.getArray(event);
        if (locs.length == 0) {
            return;
        }
        Object[] items = drops.getArray(event);
        if (items == null || items.length == 0) {
            return;
        }
        for (FabricLocation location : locs) {
            if (location.level() == null) {
                continue;
            }
            double x = location.position().x;
            double y = location.position().y;
            double z = location.position().z;
            for (Object item : items) {
                if (item instanceof FabricItemType itemType) {
                    ItemEntity itemEntity = new ItemEntity(location.level(), x, y, z, itemType.toStack());
                    if (noVelocity) {
                        itemEntity.setDeltaMovement(Vec3.ZERO);
                    }
                    location.level().addFreshEntity(itemEntity);
                    lastSpawned = itemEntity;
                } else if (item instanceof Experience experience) {
                    ExperienceOrb.award(location.level(), location.position(), experience.getXP());
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "drop " + drops.toString(event, debug) + " " + locations.toString(event, debug)
                + (noVelocity ? " without velocity" : "");
    }
}
