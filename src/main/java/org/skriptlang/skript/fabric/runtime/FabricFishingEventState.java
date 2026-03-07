package org.skriptlang.skript.fabric.runtime;

public enum FabricFishingEventState {
    FISHING("fishing line cast"),
    CAUGHT_FISH("fish caught"),
    CAUGHT_ENTITY("entity hooked"),
    IN_GROUND("bobber hit ground"),
    FISH_ESCAPE("fish escape"),
    REEL_IN("fishing rod reel in"),
    BITE("fish bite"),
    LURED("fish approaching");

    private final String displayName;

    FabricFishingEventState(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
