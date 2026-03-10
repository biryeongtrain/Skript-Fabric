package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class EvtHarvestBlock extends SkriptEvent {

    private @Nullable Literal<FabricItemType> types;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtHarvestBlock.class)) {
            return;
        }
        Skript.registerEvent(EvtHarvestBlock.class, "[player] [block|crop] harvest[ing] [of %-itemtypes%]");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        types = args.length > 0 ? (Literal<FabricItemType>) args[0] : null;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.HarvestBlock handle)) {
            return false;
        }
        if (types == null) {
            return true;
        }
        Item item = handle.blockState().getBlock().asItem();
        for (FabricItemType type : types.getAll(null)) {
            if (type != null && type.matches(new ItemStack(item, Math.max(1, type.amount())))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.HarvestBlock.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("player block harvest");
        if (types != null) {
            builder.append("of", types);
        }
        return builder.toString();
    }
}
