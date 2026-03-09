package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

public class Experience {

    private final int xp;

    public Experience() {
        xp = -1;
    }

    public Experience(int xp) {
        this.xp = xp;
    }

    public int getXP() {
        return xp == -1 ? 1 : xp;
    }

    public int getInternalXP() {
        return xp;
    }

    @Override
    public String toString() {
        return xp == -1 ? "xp" : xp + " xp";
    }

    @Override
    public int hashCode() {
        return 31 + xp;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Experience other)) {
            return false;
        }
        return xp == other.xp;
    }
}
