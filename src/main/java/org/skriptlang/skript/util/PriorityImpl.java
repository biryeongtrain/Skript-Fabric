package org.skriptlang.skript.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

final class PriorityImpl implements Priority {

    private final Set<Priority> after;
    private final Set<Priority> before;

    PriorityImpl() {
        this.after = Set.of();
        this.before = Set.of();
    }

    PriorityImpl(Priority priority, boolean isBefore) {
        Set<Priority> after = new HashSet<>();
        Set<Priority> before = new HashSet<>();
        if (isBefore) {
            before.add(priority);
        } else {
            after.add(priority);
        }
        after.addAll(priority.after());
        before.addAll(priority.before());
        this.after = Set.copyOf(after);
        this.before = Set.copyOf(before);
    }

    @Override
    public int compareTo(@NotNull Priority other) {
        if (this == other) {
            return 0;
        }

        Collection<Priority> ourBefore = this.before();
        Collection<Priority> otherAfter = other.after();
        if (ourBefore.contains(other) || otherAfter.contains(this)) {
            return -1;
        }

        Collection<Priority> ourAfter = this.after();
        Collection<Priority> otherBefore = other.before();
        if (ourAfter.contains(other) || otherBefore.contains(this)) {
            return 1;
        }

        if (ourBefore.stream().anyMatch(otherAfter::contains)) {
            return -1;
        }
        if (ourAfter.stream().anyMatch(otherBefore::contains)) {
            return 1;
        }

        return (other instanceof PriorityImpl) ? 0 : (other.compareTo(this) * -1);
    }

    @Override
    public Collection<Priority> after() {
        return after;
    }

    @Override
    public Collection<Priority> before() {
        return before;
    }

    @Override
    public int hashCode() {
        return Objects.hash(after, before);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Priority priority)) {
            return false;
        }
        return compareTo(priority) == 0;
    }
}
