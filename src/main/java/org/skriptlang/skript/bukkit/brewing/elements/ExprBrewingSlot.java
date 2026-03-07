package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprBrewingSlot extends SimpleExpression<Slot> {

    private enum BrewingSlot {
        FIRST("[brewing stand] (first|1st) bottle", "brewing stand first bottle", 0),
        SECOND("[brewing stand] (second|2nd) bottle", "brewing stand second bottle", 1),
        THIRD("[brewing stand] (third|3rd) bottle", "brewing stand third bottle", 2),
        INGREDIENT("[brewing stand] ingredient", "brewing stand ingredient", 3),
        FUEL("[brewing stand] fuel", "brewing stand fuel", 4);

        private final String pattern;
        private final String displayName;
        private final int slotIndex;

        BrewingSlot(String pattern, String displayName, int slotIndex) {
            this.pattern = pattern;
            this.displayName = displayName;
            this.slotIndex = slotIndex;
        }
    }

    private static final BrewingSlot[] BREWING_SLOTS = BrewingSlot.values();

    private BrewingSlot selectedSlot;
    private @Nullable Expression<FabricBlock> blocks;

    public static String[] patterns() {
        List<String> patterns = new ArrayList<>(BREWING_SLOTS.length * 3);
        for (BrewingSlot slot : BREWING_SLOTS) {
            patterns.add("[the] " + slot.pattern + " (slot|slots)");
            patterns.add("[the] " + slot.pattern + " (slot|slots) of %blocks%");
            patterns.add("%blocks%'s " + slot.pattern + " (slot|slots)");
        }
        return patterns.toArray(String[]::new);
    }

    @Override
    protected Slot @Nullable [] get(SkriptEvent event) {
        List<Slot> slots = new ArrayList<>();
        if (blocks == null) {
            if (event.handle() instanceof FabricBrewingFuelEventHandle handle) {
                slots.add(createSlot(handle.brewingStand()));
            }
            return slots.toArray(Slot[]::new);
        }
        for (FabricBlock block : blocks.getAll(event)) {
            if (block.level().getBlockEntity(block.position()) instanceof BrewingStandBlockEntity brewingStand) {
                slots.add(createSlot(brewingStand));
            }
        }
        return slots.toArray(Slot[]::new);
    }

    private Slot createSlot(BrewingStandBlockEntity brewingStand) {
        return new Slot(brewingStand, selectedSlot.slotIndex, 0, 0);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        selectedSlot = BREWING_SLOTS[matchedPattern / 3];
        int variant = matchedPattern % 3;
        if (variant == 0) {
            if (expressions.length != 0) {
                return false;
            }
            if (!getParser().isCurrentEvent(FabricBrewingFuelEventHandle.class)) {
                Skript.error("The event-only brewing slot syntax can only be used in a brewing fuel event.");
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
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (blocks == null) {
            return selectedSlot.displayName + " slot";
        }
        return selectedSlot.displayName + " slot of " + blocks.toString(event, debug);
    }
}
