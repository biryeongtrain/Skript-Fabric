package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectAction;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle;

public final class EvtEntityPotion extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on entity potion effect",
            "on entity potion effect modification",
            "on entity potion effect [of] %-objects%",
            "on entity potion effect [of] %-objects% %-objects%",
            "on entity potion effect modification [of] %-objects%",
            "on entity potion effect modification [of] %-objects% %-objects%",
            "on entity potion effect due to %-potioncauses%",
            "on entity potion effect modification due to %-potioncauses%",
            "on entity potion effect [of] %-objects% due to %-potioncauses%",
            "on entity potion effect [of] %-objects% %-objects% due to %-potioncauses%",
            "on entity potion effect modification [of] %-objects% due to %-potioncauses%",
            "on entity potion effect modification [of] %-objects% %-objects% due to %-potioncauses%"
    };

    private @Nullable Holder<MobEffect>[] effectTypes;
    private @Nullable FabricPotionEffectAction[] actions;
    private @Nullable FabricPotionEffectCause[] causes;

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        effectTypes = null;
        actions = null;
        causes = null;
        return switch (matchedPattern) {
            case 0, 1 -> true;
            case 2, 4 -> args.length > 0 && args[0] != null && initPrimaryFilter(args[0]);
            case 3, 5 -> initTypeAndAction(args);
            case 6, 7 -> initCauses(args, 0);
            case 8, 10 -> initPrimaryAndCauses(args);
            case 9, 11 -> initTypeActionAndCauses(args);
            default -> false;
        };
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPotionEffectEventHandle handle)) {
            return false;
        }
        if (actions != null && Arrays.stream(actions).noneMatch(action -> action == handle.action())) {
            return false;
        }
        if (causes != null && Arrays.stream(causes).noneMatch(cause -> cause == handle.cause())) {
            return false;
        }
        if (effectTypes == null || effectTypes.length == 0) {
            return true;
        }
        Holder<MobEffect> modifiedType = handle.modifiedType();
        return modifiedType != null && Arrays.stream(effectTypes).anyMatch(type -> PotionEffectSupport.sameType(type, modifiedType));
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPotionEffectEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder("on entity potion effect");
        if (effectTypes != null && effectTypes.length > 0) {
            builder.append(" of ").append(Classes.toString(effectTypes, false));
        }
        if (actions != null && actions.length > 0) {
            builder.append(' ').append(Classes.toString(actions, false));
        }
        if (causes != null && causes.length > 0) {
            builder.append(" due to ").append(joinCauses(causes));
        }
        return builder.toString();
    }

    private boolean initPrimaryFilter(Literal<?> literal) {
        FabricPotionEffectAction[] parsedActions = parseActions(literal);
        if (parsedActions != null && parsedActions.length > 0) {
            actions = parsedActions;
            return true;
        }
        Holder<MobEffect>[] parsedTypes = parseTypes(literal);
        if (parsedTypes != null && parsedTypes.length > 0) {
            effectTypes = parsedTypes;
            return true;
        }
        return false;
    }

    private boolean initTypeAndAction(Literal<?>[] args) {
        if (args.length < 2 || args[0] == null || args[1] == null) {
            return false;
        }
        effectTypes = parseTypes(args[0]);
        actions = parseActions(args[1]);
        return effectTypes != null && effectTypes.length > 0 && actions != null && actions.length > 0;
    }

    private boolean initCauses(Literal<?>[] args, int index) {
        if (args.length <= index || args[index] == null) {
            return false;
        }
        causes = parseCauses(args[index]);
        return causes != null && causes.length > 0;
    }

    private boolean initPrimaryAndCauses(Literal<?>[] args) {
        if (args.length < 2 || args[0] == null || args[1] == null) {
            return false;
        }
        return initPrimaryFilter(args[0]) && initCauses(args, 1);
    }

    private boolean initTypeActionAndCauses(Literal<?>[] args) {
        return initTypeAndAction(args) && initCauses(args, 2);
    }

    private static @Nullable Holder<MobEffect>[] parseTypes(Literal<?> literal) {
        List<Holder<MobEffect>> parsed = new ArrayList<>();
        for (Object raw : literal.getArray(null)) {
            if (raw == null) {
                continue;
            }
            Holder<MobEffect> type = PotionEffectSupport.parsePotionType(raw);
            if (type == null) {
                return null;
            }
            parsed.add(type);
        }
        return parsed.isEmpty() ? null : parsed.toArray(Holder[]::new);
    }

    private static @Nullable FabricPotionEffectAction[] parseActions(Literal<?> literal) {
        List<FabricPotionEffectAction> parsed = new ArrayList<>();
        for (Object raw : literal.getArray(null)) {
            if (raw == null) {
                continue;
            }
            FabricPotionEffectAction action = FabricPotionEffectAction.parse(raw);
            if (action == null) {
                return null;
            }
            parsed.add(action);
        }
        return parsed.isEmpty() ? null : parsed.toArray(FabricPotionEffectAction[]::new);
    }

    private static @Nullable FabricPotionEffectCause[] parseCauses(Literal<?> literal) {
        List<FabricPotionEffectCause> parsed = new ArrayList<>();
        for (Object raw : literal.getArray(null)) {
            if (raw == null) {
                continue;
            }
            FabricPotionEffectCause cause = FabricPotionEffectCause.parse(raw);
            if (cause == null) {
                return null;
            }
            parsed.add(cause);
        }
        return parsed.isEmpty() ? null : parsed.toArray(FabricPotionEffectCause[]::new);
    }

    private static String joinCauses(FabricPotionEffectCause[] causes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < causes.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(causes[i].skriptName());
        }
        return builder.toString();
    }
}
