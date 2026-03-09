package ch.njol.skript.log;

import java.util.Iterator;
import java.util.LinkedList;
import org.jetbrains.annotations.Nullable;

public class HandlerList implements Iterable<LogHandler> {

    private final LinkedList<LogHandler> list = new LinkedList<>();

    public void add(LogHandler handler) {
        list.addFirst(handler);
    }

    public @Nullable LogHandler remove() {
        return list.isEmpty() ? null : list.removeFirst();
    }

    @Override
    public Iterator<LogHandler> iterator() {
        return list.iterator();
    }

    public boolean contains(LogHandler handler) {
        return list.contains(handler);
    }
}
