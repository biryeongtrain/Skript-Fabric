package org.skriptlang.skript.bukkit.furnace.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.FabricFurnaceEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFurnaceSlot extends SimpleExpression<Slot> {

    private enum FurnaceSlot {
        INPUT("input", 0),
        FUEL("fuel", 1),
        OUTPUT("output", 2);

        private final String display;
        private final int index;

        FurnaceSlot(String display, int index) {
            this.display = display;
            this.index = index;
        }
    }

    private FurnaceSlot selectedSlot;
    private @Nullable Expression<FabricBlock> blocks;

    public static String[] patterns() {
        List<String> patterns = new ArrayList<>();
        for (FurnaceSlot slot : FurnaceSlot.values()) {
            patterns.add("[the] " + slot.display + " slot[s]");
            patterns.add("[the] " + slot.display + " slot[s] of %blocks%");
            patterns.add("%blocks%'[s] " + slot.display + " slot[s]");
        }
        return patterns.toArray(String[]::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        selectedSlot = FurnaceSlot.values()[matchedPattern / 3];
        if (matchedPattern % 3 == 0) {
            if (!getParser().isCurrentEvent(FabricFurnaceEventHandle.class)) {
                Skript.error("The event-only furnace slot syntax can only be used in furnace events.");
                return false;
            }
            blocks = null;
            return true;
        }
        if (expressions.length != 1 || !expressions[0].canReturn(FabricBlock.class)) {
            return false;
        }
        blocks = (Expression<FabricBlock>) expressions[0];
        return true;
    }

    @Override
    protected Slot @Nullable [] get(SkriptEvent event) {
        List<Slot> results = new ArrayList<>();
        if (blocks == null) {
            if (event.handle() instanceof FabricFurnaceEventHandle handle) {
                results.add(new Slot(handle.furnace(), selectedSlot.index, 0, 0));
            }
            return results.toArray(Slot[]::new);
        }
        for (FabricBlock block : blocks.getAll(event)) {
            if (block.level().getBlockEntity(block.position()) instanceof AbstractFurnaceBlockEntity furnace) {
                results.add(new Slot(furnace, selectedSlot.index, 0, 0));
            }
        }
        return results.toArray(Slot[]::new);
    }

    @Override
    public boolean isSingle() {
        return blocks == null || blocks.isSingle();
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return selectedSlot.display + " slot";
    }
}
