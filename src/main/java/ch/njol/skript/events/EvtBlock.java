package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@SuppressWarnings("unchecked")
public final class EvtBlock extends SkriptEvent {

    private @Nullable Literal<Object> types;
    private FabricEventCompatHandles.BlockAction action;
    private boolean mine;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtBlock.class)) {
            return;
        }
        Skript.registerEvent(
                EvtBlock.class,
                "[block] (break[ing]|1¦min(e|ing)) [[of] %-itemtypes/blockdatas%]",
                "[block] burn[ing] [[of] %-itemtypes/blockdatas%]",
                "[block] (plac(e|ing)|build[ing]) [[of] %-itemtypes/blockdatas%]",
                "[block] fad(e|ing) [[of] %-itemtypes/blockdatas%]",
                "[block] form[ing] [[of] %-itemtypes/blockdatas%]",
                "block drop[ping] [[of] %-itemtypes/blockdatas%]"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        types = args.length > 0 ? (Literal<Object>) args[0] : null;
        action = switch (matchedPattern) {
            case 1 -> FabricEventCompatHandles.BlockAction.BURN;
            case 2 -> FabricEventCompatHandles.BlockAction.PLACE;
            case 3 -> FabricEventCompatHandles.BlockAction.FADE;
            case 4 -> FabricEventCompatHandles.BlockAction.FORM;
            case 5 -> FabricEventCompatHandles.BlockAction.DROP;
            default -> FabricEventCompatHandles.BlockAction.BREAK;
        };
        mine = matchedPattern == 0 && parser.mark == 1;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Block handle)) {
            return false;
        }
        if (handle.action() != action) {
            return false;
        }
        if (mine && !handle.dropped()) {
            return false;
        }
        if (types == null) {
            return true;
        }
        return types.check(event, candidate -> matchesType(candidate, handle.blockState(), handle.itemStack()));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Block.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(action == FabricEventCompatHandles.BlockAction.BREAK && mine ? "mine" : action.name().toLowerCase());
        if (types != null) {
            builder.append("of", types);
        }
        return builder.toString();
    }

    private static boolean matchesType(@Nullable Object candidate, @Nullable BlockState blockState, @Nullable ItemStack itemStack) {
        if (candidate instanceof FabricItemType itemType) {
            ItemStack stack = itemStack;
            if (stack == null && blockState != null) {
                Item item = blockState.getBlock().asItem();
                stack = item == null ? null : new ItemStack(item, Math.max(1, itemType.amount()));
            }
            return stack != null && itemType.matches(stack);
        }
        return candidate instanceof BlockState state && blockState != null && Objects.equals(blockState, state);
    }
}
