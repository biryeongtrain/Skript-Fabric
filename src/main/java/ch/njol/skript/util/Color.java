package ch.njol.skript.util;

public interface Color {

    int red();

    int green();

    int blue();

    default int rgb() {
        return ((red() & 0xFF) << 16) | ((green() & 0xFF) << 8) | (blue() & 0xFF);
    }
}
