package ch.njol.skript.classes;

import ch.njol.skript.lang.DefaultExpression;
import org.skriptlang.skript.lang.comparator.Relation;

/**
 * Convenience class info for enum-backed compatibility registrations.
 *
 * <p>The upstream implementation also wires serializers and localized event
 * values. The local port keeps the parser, supplier, usage strings, and default
 * comparator path that upstream imports expect inside Lane A ownership.
 */
public class EnumClassInfo<T extends Enum<T>> extends ClassInfo<T> {

    public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode) {
        this(enumClass, codeName, languageNode, defaultExpression(enumClass), true);
    }

    public EnumClassInfo(Class<T> enumClass, String codeName, String languageNode, boolean registerComparator) {
        this(enumClass, codeName, languageNode, defaultExpression(enumClass), registerComparator);
    }

    public EnumClassInfo(
            Class<T> enumClass,
            String codeName,
            String languageNode,
            DefaultExpression<T> defaultExpression
    ) {
        this(enumClass, codeName, languageNode, defaultExpression, true);
    }

    public EnumClassInfo(
            Class<T> enumClass,
            String codeName,
            String languageNode,
            DefaultExpression<T> defaultExpression,
            boolean registerComparator
    ) {
        super(enumClass, codeName);
        EnumParser<T> enumParser = new EnumParser<>(enumClass, languageNode);
        usage(enumParser.getCombinedPatterns())
                .defaultExpression(defaultExpression)
                .supplier(() -> java.util.Arrays.asList(enumClass.getEnumConstants()).iterator())
                .parser(enumParser);

        if (registerComparator) {
            ch.njol.skript.registrations.Comparators.registerComparator(
                    enumClass,
                    enumClass,
                    (first, second) -> Relation.get(first.ordinal() - second.ordinal())
            );
        }
    }

    private static <T extends Enum<T>> DefaultExpression<T> defaultExpression(Class<T> enumClass) {
        return new DefaultExpression<>() {
            @Override
            public boolean init() {
                return true;
            }

            @Override
            public boolean isDefault() {
                return true;
            }

            @Override
            public Class<? extends T> getReturnType() {
                return enumClass;
            }

            @Override
            public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
                return codeName(enumClass);
            }
        };
    }

    private static String codeName(Class<? extends Enum<?>> enumClass) {
        return enumClass.getSimpleName().replaceAll("[^A-Za-z0-9]", "").toLowerCase(java.util.Locale.ENGLISH);
    }
}
