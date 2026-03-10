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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Apply Bone Meal")
@Description("Applies bone meal to a crop, sapling, or composter")
@Example("apply 3 bone meal to event-block")
@RequiredPlugins("MC 1.16.2+")
@Since("2.8.0")
public final class EffApplyBoneMeal extends Effect {

    private static boolean registered;

    private @Nullable Expression<Number> amount;
    private Expression<FabricBlock> blocks;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffApplyBoneMeal.class, "apply [%-number%] bone[ ]meal[s] [to %blocks%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        amount = (Expression<Number>) exprs[0];
        blocks = (Expression<FabricBlock>) exprs[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        int times = amount == null ? 1 : Math.max(0, amount.getOptionalSingle(event).orElse(0).intValue());
        if (times == 0) {
            return;
        }
        for (FabricBlock block : blocks.getArray(event)) {
            BlockPos position = block.position();
            for (int i = 0; i < times; i++) {
                BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), block.level(), position);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "apply " + (amount != null ? amount.toString(event, debug) + " " : "") + "bone meal to "
                + blocks.toString(event, debug);
    }
}
