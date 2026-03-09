package ch.njol.skript.registrations;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.ComparatorInfo;
import org.skriptlang.skript.lang.comparator.Relation;

import java.util.List;

/**
 * Legacy comparator registry bridge kept on top of the current comparator backend.
 */
@Deprecated(since = "2.10.0", forRemoval = true)
@SuppressWarnings("removal")
public final class Comparators {

    private Comparators() {
    }

    public static List<ComparatorInfo<?, ?>> getComparators() {
        return org.skriptlang.skript.lang.comparator.Comparators.getComparatorInfos();
    }

    public static <T1, T2> void registerComparator(
            Class<T1> firstType,
            Class<T2> secondType,
            Comparator<T1, T2> comparator
    ) {
        org.skriptlang.skript.lang.comparator.Comparators.registerComparator(firstType, secondType, comparator);
    }

    public static boolean exactComparatorExists(Class<?> firstType, Class<?> secondType) {
        return org.skriptlang.skript.lang.comparator.Comparators.exactComparatorExists(firstType, secondType);
    }

    public static boolean comparatorExists(Class<?> firstType, Class<?> secondType) {
        return org.skriptlang.skript.lang.comparator.Comparators.comparatorExists(firstType, secondType);
    }

    public static <T1, T2> Relation compare(@Nullable T1 first, @Nullable T2 second) {
        return org.skriptlang.skript.lang.comparator.Comparators.compare(first, second);
    }

    public static <T1, T2> @Nullable Comparator<T1, T2> getComparator(Class<T1> firstType, Class<T2> secondType) {
        return org.skriptlang.skript.lang.comparator.Comparators.getComparator(firstType, secondType);
    }

    public static <T1, T2> @Nullable ComparatorInfo<T1, T2> getComparatorInfo(
            Class<T1> firstType,
            Class<T2> secondType
    ) {
        return org.skriptlang.skript.lang.comparator.Comparators.getComparatorInfo(firstType, secondType);
    }
}
