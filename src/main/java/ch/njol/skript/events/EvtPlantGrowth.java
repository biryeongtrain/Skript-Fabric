package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class EvtPlantGrowth extends SkriptEvent {

    private @Nullable Literal<FabricItemType> types;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtPlantGrowth.class)) {
            return;
        }
        Skript.registerEvent(EvtPlantGrowth.class, "(plant|crop|block) grow[(th|ing)] [[of] %-itemtypes%]");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        types = args.length > 0 ? (Literal<FabricItemType>) args[0] : null;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.PlantGrowth handle)) {
            return false;
        }
        if (types == null || handle.from() == null) {
            return true;
        }
        Item item = handle.from().getBlock().asItem();
        if (item == null) {
            return false;
        }
        for (FabricItemType type : types.getAll(null)) {
            if (type != null && type.matches(new ItemStack(item, Math.max(1, type.amount())))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.PlantGrowth.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "plant growth";
    }
}
