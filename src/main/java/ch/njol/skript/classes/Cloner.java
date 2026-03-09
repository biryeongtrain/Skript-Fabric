package ch.njol.skript.classes;

@FunctionalInterface
public interface Cloner<T> {

    T clone(T value);
}
