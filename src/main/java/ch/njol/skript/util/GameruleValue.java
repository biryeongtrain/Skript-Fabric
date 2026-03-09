package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

public class GameruleValue<T> {

    private T gameruleValue;

    public GameruleValue(T gameruleValue) {
        this.gameruleValue = gameruleValue;
    }

    public T getGameruleValue() {
        return gameruleValue;
    }

    @Override
    public String toString() {
        return gameruleValue.toString();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (!(other instanceof GameruleValue<?> gameruleValue)) {
            return false;
        }
        return this.gameruleValue.equals(gameruleValue.gameruleValue);
    }
}
