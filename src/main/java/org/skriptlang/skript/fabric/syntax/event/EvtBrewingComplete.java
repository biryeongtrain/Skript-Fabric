package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.FabricBrewingCompleteEventHandle;

public final class EvtBrewingComplete extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on brew complete",
            "on brew complete [(of|for)] %-objects%",
            "on brew completed",
            "on brew completed [(of|for)] %-objects%",
            "on brew completion",
            "on brew completion [(of|for)] %-objects%",
            "on brew finish",
            "on brew finish [(of|for)] %-objects%",
            "on brew finished",
            "on brew finished [(of|for)] %-objects%",
            "on brewing complete",
            "on brewing complete [(of|for)] %-objects%",
            "on brewing completed",
            "on brewing completed [(of|for)] %-objects%",
            "on brewing completion",
            "on brewing completion [(of|for)] %-objects%",
            "on brewing finish",
            "on brewing finish [(of|for)] %-objects%",
            "on brewing finished",
            "on brewing finished [(of|for)] %-objects%"
    };

    private @Nullable FabricItemType[] itemTypes;
    private @Nullable Holder<MobEffect>[] effectTypes;

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        itemTypes = null;
        effectTypes = null;
        if ((matchedPattern & 1) == 0 || args.length == 0 || args[0] == null) {
            return true;
        }
        ParsedFilters filters = parseFilters(args[0]);
        if (filters == null) {
            return false;
        }
        itemTypes = filters.itemTypes();
        effectTypes = filters.effectTypes();
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricBrewingCompleteEventHandle handle)) {
            return false;
        }
        if ((itemTypes == null || itemTypes.length == 0) && (effectTypes == null || effectTypes.length == 0)) {
            return true;
        }
        for (ItemStack result : handle.results()) {
            if (matchesItemType(result) || matchesPotionEffect(result)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricBrewingCompleteEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        if ((itemTypes == null || itemTypes.length == 0) && (effectTypes == null || effectTypes.length == 0)) {
            return "on brewing complete";
        }
        List<Object> filters = new ArrayList<>();
        if (itemTypes != null) {
            filters.addAll(Arrays.asList(itemTypes));
        }
        if (effectTypes != null) {
            filters.addAll(Arrays.asList(effectTypes));
        }
        return "on brewing complete for " + Classes.toString(filters.toArray(), false);
    }

    private boolean matchesItemType(ItemStack result) {
        if (itemTypes == null || itemTypes.length == 0) {
            return false;
        }
        return Arrays.stream(itemTypes).anyMatch(itemType -> itemType.matches(result));
    }

    private boolean matchesPotionEffect(ItemStack result) {
        if (effectTypes == null || effectTypes.length == 0) {
            return false;
        }
        PotionContents contents = result.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return false;
        }
        for (MobEffectInstance effect : contents.getAllEffects()) {
            Holder<MobEffect> resultType = effect.getEffect();
            if (Arrays.stream(effectTypes).anyMatch(type -> PotionEffectSupport.sameType(type, resultType))) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable ParsedFilters parseFilters(Literal<?> literal) {
        List<FabricItemType> itemTypes = new ArrayList<>();
        List<Holder<MobEffect>> effectTypes = new ArrayList<>();
        for (Object raw : literal.getArray(null)) {
            if (raw == null) {
                continue;
            }
            FabricItemType itemType = parseItemType(raw);
            if (itemType != null) {
                itemTypes.add(itemType);
                continue;
            }
            Holder<MobEffect> effectType = PotionEffectSupport.parsePotionType(raw);
            if (effectType == null) {
                return null;
            }
            effectTypes.add(effectType);
        }
        if (itemTypes.isEmpty() && effectTypes.isEmpty()) {
            return null;
        }
        return new ParsedFilters(
                itemTypes.isEmpty() ? null : itemTypes.toArray(FabricItemType[]::new),
                effectTypes.isEmpty() ? null : effectTypes.toArray(Holder[]::new)
        );
    }

    private static @Nullable FabricItemType parseItemType(Object raw) {
        if (raw instanceof FabricItemType direct) {
            return direct;
        }
        return Classes.parse(String.valueOf(raw), FabricItemType.class, ParseContext.DEFAULT);
    }

    private record ParsedFilters(
            @Nullable FabricItemType[] itemTypes,
            @Nullable Holder<MobEffect>[] effectTypes
    ) {
    }
}
