package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventState;

public final class EvtFishing extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on fishing",
            "on [fishing] (line|rod) cast",
            "on fish (caught|catch)",
            "on entity (hook[ed]|caught|catch)",
            "on (bobber|hook) (in|hit) ground",
            "on fish (escape|get away)",
            "on [fishing] (rod|line) reel in",
            "on fish bit(e|ing)",
            "on (fish approach[ing]|(bobber|hook) lure[d])",
            "on fishing state change[d]"
    };

    private @Nullable FabricFishingEventState state;
    private boolean stateChange;

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (args.length != 0) {
            return false;
        }
        stateChange = matchedPattern == PATTERNS.length - 1;
        state = switch (matchedPattern) {
            case 0 -> null;
            case 1 -> FabricFishingEventState.FISHING;
            case 2 -> FabricFishingEventState.CAUGHT_FISH;
            case 3 -> FabricFishingEventState.CAUGHT_ENTITY;
            case 4 -> FabricFishingEventState.IN_GROUND;
            case 5 -> FabricFishingEventState.FISH_ESCAPE;
            case 6 -> FabricFishingEventState.REEL_IN;
            case 7 -> FabricFishingEventState.BITE;
            case 8 -> FabricFishingEventState.LURED;
            case 9 -> null;
            default -> throw new IllegalStateException("Unexpected fishing pattern index: " + matchedPattern);
        };
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return false;
        }
        return stateChange || state == null || handle.state() == state;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricFishingEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        if (stateChange) {
            return "on fishing state change";
        }
        return state == null ? "on fishing" : "on " + state.displayName();
    }
}
