package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@SuppressWarnings("unchecked")
public final class EvtGrow extends SkriptEvent {

    private static final int ANY = 0;
    private static final int STRUCTURE = 1;
    private static final int BLOCK = 2;

    private static final int OF = 0;
    private static final int FROM = 1;
    private static final int INTO = 2;
    private static final int FROM_INTO = 3;

    private @Nullable Literal<Object> toTypes;
    private @Nullable Literal<Object> fromTypes;
    private int eventRestriction;
    private int actionRestriction;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtGrow.class)) {
            return;
        }
        Skript.registerEvent(
                EvtGrow.class,
                "grow[th] [of (1:%-structuretypes%|2:%-itemtypes/blockdatas%)]",
                "grow[th] from %itemtypes/blockdatas%",
                "grow[th] [in]to (1:%structuretypes%|2:%itemtypes/blockdatas%)",
                "grow[th] from %itemtypes/blockdatas% [in]to (1:%structuretypes%|2:%itemtypes/blockdatas%)"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        eventRestriction = parseResult.mark;
        actionRestriction = matchedPattern;
        switch (actionRestriction) {
            case OF -> fromTypes = eventRestriction == STRUCTURE ? (Literal<Object>) args[0]
                    : eventRestriction == BLOCK ? (Literal<Object>) args[1] : null;
            case FROM -> fromTypes = (Literal<Object>) args[0];
            case INTO -> toTypes = eventRestriction == STRUCTURE ? (Literal<Object>) args[0] : (Literal<Object>) args[1];
            case FROM_INTO -> {
                fromTypes = (Literal<Object>) args[0];
                toTypes = eventRestriction == STRUCTURE ? (Literal<Object>) args[1] : (Literal<Object>) args[2];
            }
            default -> {
                return false;
            }
        }
        invertAndList(fromTypes);
        invertAndList(toTypes);
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Grow handle)) {
            return false;
        }
        return switch (actionRestriction) {
            case OF -> fromTypes == null || checkFrom(event, handle, fromTypes) || checkTo(event, handle, fromTypes);
            case FROM -> fromTypes != null && checkFrom(event, handle, fromTypes);
            case INTO -> toTypes != null && checkTo(event, handle, toTypes);
            case FROM_INTO -> fromTypes != null && toTypes != null
                    && checkFrom(event, handle, fromTypes)
                    && checkTo(event, handle, toTypes);
            default -> false;
        };
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Grow.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        if (fromTypes == null && toTypes == null) {
            return "grow";
        }
        return switch (actionRestriction) {
            case OF -> "grow of " + fromTypes.toString(event, debug);
            case FROM -> "grow from " + fromTypes.toString(event, debug);
            case INTO -> "grow into " + toTypes.toString(event, debug);
            case FROM_INTO -> "grow from " + fromTypes.toString(event, debug) + " into " + toTypes.toString(event, debug);
            default -> "grow";
        };
    }

    private static void invertAndList(@Nullable Literal<Object> types) {
        if (types instanceof LiteralList<Object> list && list.getAnd()) {
            list.invertAnd();
        }
    }

    private boolean checkFrom(org.skriptlang.skript.lang.event.SkriptEvent event, FabricEventCompatHandles.Grow handle, Literal<Object> types) {
        if (eventRestriction == STRUCTURE) {
            return handle.structureType() != null
                    && types.check(event, type -> matchesStructure(type, handle.structureType()));
        }
        return types.check(event, type -> matchesBlockType(type, handle.from()));
    }

    private boolean checkTo(org.skriptlang.skript.lang.event.SkriptEvent event, FabricEventCompatHandles.Grow handle, Literal<Object> types) {
        if (eventRestriction == STRUCTURE) {
            return handle.structureType() != null
                    && types.check(event, type -> matchesStructure(type, handle.structureType()));
        }
        return types.check(event, type -> matchesBlockType(type, handle.to()));
    }

    private static boolean matchesBlockType(@Nullable Object type, @Nullable BlockState state) {
        if (state == null) {
            return false;
        }
        if (type instanceof FabricItemType itemType) {
            Item item = state.getBlock().asItem();
            return item != null && itemType.matches(new ItemStack(item, Math.max(1, itemType.amount())));
        }
        return type instanceof BlockState blockState && state.equals(blockState);
    }

    private static boolean matchesStructure(@Nullable Object type, @Nullable String structureType) {
        return type != null && structureType != null
                && structureType.equalsIgnoreCase(type.toString().toLowerCase(Locale.ENGLISH));
    }
}
