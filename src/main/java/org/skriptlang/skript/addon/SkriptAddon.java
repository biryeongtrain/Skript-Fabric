package org.skriptlang.skript.addon;

public interface SkriptAddon {

    String name();

    <T> T registry(Class<T> type);
}
