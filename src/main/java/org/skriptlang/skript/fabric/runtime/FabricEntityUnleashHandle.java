package org.skriptlang.skript.fabric.runtime;

public final class FabricEntityUnleashHandle implements FabricEntityUnleashEventHandle {

    private boolean dropLeash;

    public FabricEntityUnleashHandle(boolean dropLeash) {
        this.dropLeash = dropLeash;
    }

    public boolean isDropLeash() {
        return dropLeash;
    }

    @Override
    public void setDropLeash(boolean dropLeash) {
        this.dropLeash = dropLeash;
    }
}
