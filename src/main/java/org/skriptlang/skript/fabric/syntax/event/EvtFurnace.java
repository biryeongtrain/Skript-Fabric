package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.FabricFurnaceEventHandle;

public final class EvtFurnace extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on [furnace] [ore] smelt",
            "on [furnace] [ore] smelting",
            "on [furnace] [ore] smelt of %objects%",
            "on [furnace] [ore] smelting of %objects%",
            "on [furnace] smelt[ed|ing] of ore",
            "on [furnace] fuel burn",
            "on [furnace] fuel burning",
            "on [furnace] fuel burn of %objects%",
            "on [furnace] fuel burning of %objects%",
            "on [furnace] start [of] smelt",
            "on [furnace] start [of] smelting",
            "on [furnace] start [of] smelt of %objects%",
            "on [furnace] start [of] smelting of %objects%",
            "on [furnace] smelt start",
            "on [furnace] smelting start",
            "on [furnace] smelt start of %objects%",
            "on [furnace] smelting start of %objects%",
            "on furnace [item] extract",
            "on furnace [item] extraction",
            "on furnace [item] extract of %objects%",
            "on furnace [item] extraction of %objects%"
    };

    private FabricFurnaceEventHandle.Kind kind;
    private @Nullable FabricItemType[] itemTypes;

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        kind = switch (matchedPattern) {
            case 0, 1, 2, 3, 4 -> FabricFurnaceEventHandle.Kind.SMELT;
            case 5, 6, 7, 8 -> FabricFurnaceEventHandle.Kind.BURN;
            case 9, 10, 11, 12, 13, 14, 15, 16 -> FabricFurnaceEventHandle.Kind.START_SMELT;
            case 17, 18, 19, 20 -> FabricFurnaceEventHandle.Kind.EXTRACT;
            default -> FabricFurnaceEventHandle.Kind.SMELT;
        };
        if (!hasItemFilter(matchedPattern)) {
            itemTypes = null;
            return args.length == 0;
        }
        if (args.length != 1 || args[0] == null) {
            return false;
        }
        itemTypes = parseItemTypes(args[0]);
        return itemTypes != null && itemTypes.length > 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricFurnaceEventHandle handle) || handle.kind() != kind) {
            return false;
        }
        if (itemTypes == null || itemTypes.length == 0) {
            return true;
        }
        ItemStack candidate = switch (kind) {
            case BURN -> handle.fuel();
            case EXTRACT -> handle.result();
            case SMELT, START_SMELT -> handle.source();
        };
        for (FabricItemType itemType : itemTypes) {
            if (itemType.matches(candidate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricFurnaceEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        String base = switch (kind) {
            case SMELT -> "on furnace smelt";
            case BURN -> "on fuel burn";
            case START_SMELT -> "on smelting start";
            case EXTRACT -> "on furnace extract";
        };
        return itemTypes == null || itemTypes.length == 0
                ? base
                : base + " of " + Classes.toString(itemTypes, false);
    }

    private static boolean hasItemFilter(int matchedPattern) {
        return matchedPattern == 2
                || matchedPattern == 3
                || matchedPattern == 7
                || matchedPattern == 8
                || matchedPattern == 11
                || matchedPattern == 12
                || matchedPattern == 15
                || matchedPattern == 16
                || matchedPattern == 19
                || matchedPattern == 20;
    }

    private static @Nullable FabricItemType[] parseItemTypes(Literal<?> literal) {
        List<FabricItemType> parsed = new ArrayList<>();
        for (Object raw : literal.getArray(null)) {
            if (raw == null) {
                continue;
            }
            FabricItemType itemType = raw instanceof FabricItemType direct
                    ? direct
                    : Classes.parse(String.valueOf(raw), FabricItemType.class, ParseContext.DEFAULT);
            if (itemType == null) {
                return null;
            }
            parsed.add(itemType);
        }
        return parsed.isEmpty() ? null : parsed.toArray(FabricItemType[]::new);
    }
}
