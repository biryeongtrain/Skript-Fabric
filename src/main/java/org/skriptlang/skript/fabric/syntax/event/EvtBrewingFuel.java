package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelEventHandle;

public final class EvtBrewingFuel extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on brewing fuel",
            "on brewing fuel [of] %-objects%",
            "on brew[ing [stand]] consum(e|ing) fuel",
            "on brew[ing [stand]] consum(e|ing) fuel [of] %-objects%",
            "on brew[ing [stand]] fuel consumption",
            "on brew[ing [stand]] fuel consumption [of] %-objects%"
    };

    private @Nullable FabricItemType[] itemTypes;

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if ((matchedPattern & 1) == 0 || args.length == 0 || args[0] == null) {
            itemTypes = null;
            return true;
        }
        itemTypes = parseItemTypes(args[0]);
        return itemTypes != null && itemTypes.length > 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricBrewingFuelEventHandle handle)) {
            return false;
        }
        if (itemTypes == null || itemTypes.length == 0) {
            return true;
        }
        ItemStack fuel = handle.brewingStand().getItem(4);
        for (FabricItemType itemType : itemTypes) {
            if (itemType.matches(fuel)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricBrewingFuelEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return itemTypes == null || itemTypes.length == 0
                ? "on brewing fuel"
                : "on brewing fuel of " + Classes.toString(itemTypes, false);
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
