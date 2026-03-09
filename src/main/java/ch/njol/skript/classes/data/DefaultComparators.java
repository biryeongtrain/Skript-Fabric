package ch.njol.skript.classes.data;

import ch.njol.skript.registrations.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

/**
 * Java-only subset of upstream default comparators.
 */
public final class DefaultComparators {

    private DefaultComparators() {
    }

    public static void register() {
        if (Comparators.exactComparatorExists(Number.class, Number.class)) {
            return;
        }
        Comparators.registerComparator(Number.class, Number.class, new org.skriptlang.skript.lang.comparator.Comparator<>() {
            @Override
            public Relation compare(Number first, Number second) {
                if (first instanceof Long && second instanceof Long) {
                    return Relation.get(first.longValue() - second.longValue());
                }
                double left = first.doubleValue();
                double right = second.doubleValue();
                if (Double.isNaN(left) || Double.isNaN(right)) {
                    return Relation.SMALLER;
                }
                if (Double.isInfinite(left) || Double.isInfinite(right)) {
                    return left > right ? Relation.GREATER : left < right ? Relation.SMALLER : Relation.EQUAL;
                }
                double epsilon = first instanceof Float || second instanceof Float
                        ? Math.min(Math.abs(left), Math.abs(right)) * 1.0E-6
                        : 1.0E-10;
                if (Math.abs(left - right) < epsilon) {
                    return Relation.EQUAL;
                }
                return Relation.get(left - right);
            }

            @Override
            public boolean supportsOrdering() {
                return true;
            }
        });
    }
}
