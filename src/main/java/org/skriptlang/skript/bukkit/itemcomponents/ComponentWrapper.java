package org.skriptlang.skript.bukkit.itemcomponents;

public abstract class ComponentWrapper<T> {

    public abstract T getComponent();

    public abstract void applyComponent(T component);

    public abstract ComponentWrapper<T> copy();

    @Override
    public String toString() {
        return String.valueOf(getComponent());
    }
}
