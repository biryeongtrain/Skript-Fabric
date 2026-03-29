package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@SuppressWarnings("unchecked")
public final class EvtBlock extends SkriptEvent {

    private static final @Nullable Class<?> HANGING_BREAK_EVENT = resolveEffectHandleClass("HangingBreak");
    private static final @Nullable Class<?> HANGING_PLACE_EVENT = resolveEffectHandleClass("HangingPlace");

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
        Object handle = event.handle();
        if (handle instanceof FabricEventCompatHandles.Block blockHandle) {
            if (blockHandle.action() != action) {
                return false;
            }
            if (mine && !blockHandle.dropped()) {
                return false;
            }
            if (types == null) {
                return true;
            }
            return types.check(event, candidate -> matchesType(candidate, blockHandle.blockState(), blockHandle.itemStack(), null));
        }
        if (!isMatchingHangingHandle(handle, action)) {
            return false;
        }
        if (mine) {
            return false;
        }
        if (types == null) {
            return true;
        }
        Entity entity = hangingEntity(handle);
        return types.check(event, candidate -> matchesType(candidate, null, null, entity));
    }

    @Override
    public Class<?>[] getEventClasses() {
        List<Class<?>> eventClasses = new ArrayList<>();
        eventClasses.add(FabricEventCompatHandles.Block.class);
        if (action == FabricEventCompatHandles.BlockAction.BREAK && HANGING_BREAK_EVENT != null) {
            eventClasses.add(HANGING_BREAK_EVENT);
        }
        if (action == FabricEventCompatHandles.BlockAction.PLACE && HANGING_PLACE_EVENT != null) {
            eventClasses.add(HANGING_PLACE_EVENT);
        }
        return eventClasses.toArray(Class<?>[]::new);
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

    private static boolean matchesType(
            @Nullable Object candidate,
            @Nullable BlockState blockState,
            @Nullable ItemStack itemStack,
            @Nullable Entity hangingEntity
    ) {
        if (candidate instanceof FabricItemType itemType) {
            ItemStack stack = itemStack;
            if (stack == null && blockState != null) {
                Item item = blockState.getBlock().asItem();
                stack = item == null ? null : new ItemStack(item, Math.max(1, itemType.amount()));
            }
            if (stack == null && hangingEntity != null) {
                stack = hangingPickResult(hangingEntity, itemType.amount());
            }
            return stack != null && itemType.matches(stack);
        }
        if (candidate instanceof EntityData<?> entityData && hangingEntity != null) {
            return entityData.isInstance(hangingEntity);
        }
        if (hangingEntity != null) {
            String candidateName = String.valueOf(candidate);
            String hangingName = hangingEventName(hangingEntity);
            if (!hangingName.isEmpty() && hangingName.equalsIgnoreCase(candidateName)) {
                return true;
            }
        }
        return candidate instanceof BlockState state && blockState != null && Objects.equals(blockState, state);
    }

    private static boolean isMatchingHangingHandle(Object handle, FabricEventCompatHandles.BlockAction action) {
        return (action == FabricEventCompatHandles.BlockAction.BREAK && HANGING_BREAK_EVENT != null && HANGING_BREAK_EVENT.isInstance(handle))
                || (action == FabricEventCompatHandles.BlockAction.PLACE && HANGING_PLACE_EVENT != null && HANGING_PLACE_EVENT.isInstance(handle));
    }

    private static @Nullable Entity hangingEntity(Object handle) {
        try {
            Method method = handle.getClass().getMethod("entity");
            Object value = method.invoke(handle);
            return value instanceof Entity entity ? entity : null;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static @Nullable ItemStack hangingPickResult(Entity entity, int amount) {
        ItemStack stack = null;
        if (entity instanceof ItemFrame itemFrame) {
            stack = new ItemStack(itemFrame.getType() == EntityType.GLOW_ITEM_FRAME ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME);
        } else if (entity instanceof Painting painting) {
            stack = new ItemStack(Items.PAINTING);
        }
        return stack == null || stack.isEmpty() ? null : stack.copyWithCount(Math.max(1, amount));
    }

    private static String hangingEventName(Entity entity) {
        if (entity instanceof ItemFrame itemFrame) {
            return itemFrame.getType() == EntityType.GLOW_ITEM_FRAME ? "glow item frame" : "item frame";
        }
        if (entity instanceof Painting) {
            return "painting";
        }
        return "";
    }

    private static @Nullable Class<?> resolveEffectHandleClass(String simpleName) {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$" + simpleName);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }
}
