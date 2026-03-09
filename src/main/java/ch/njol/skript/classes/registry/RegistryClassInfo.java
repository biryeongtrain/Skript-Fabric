package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.DefaultExpression;
import java.util.Locale;
import net.minecraft.core.Registry;
import org.skriptlang.skript.lang.comparator.Relation;

/**
 * Convenience class info for registry-backed Fabric compatibility types.
 *
 * <p>The local port intentionally omits the upstream serializer hookup until
 * the Yggdrasil-backed serializer surface exists again.
 */
public class RegistryClassInfo<R> extends ClassInfo<R> {

    public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode) {
        this(registryClass, registry, codeName, languageNode, defaultExpression(registryClass), true);
    }

    public RegistryClassInfo(
            Class<R> registryClass,
            Registry<R> registry,
            String codeName,
            String languageNode,
            boolean registerComparator
    ) {
        this(registryClass, registry, codeName, languageNode, defaultExpression(registryClass), registerComparator);
    }

    public RegistryClassInfo(
            Class<R> registryClass,
            Registry<R> registry,
            String codeName,
            String languageNode,
            DefaultExpression<R> defaultExpression
    ) {
        this(registryClass, registry, codeName, languageNode, defaultExpression, true);
    }

    public RegistryClassInfo(
            Class<R> registryClass,
            Registry<R> registry,
            String codeName,
            String languageNode,
            DefaultExpression<R> defaultExpression,
            boolean registerComparator
    ) {
        super(registryClass, codeName);
        RegistryParser<R> registryParser = new RegistryParser<>(registry, languageNode);
        usage(registryParser.getCombinedPatterns())
                .supplier(registry::iterator)
                .defaultExpression(defaultExpression)
                .parser(registryParser);

        if (registerComparator) {
            ch.njol.skript.registrations.Comparators.registerComparator(
                    registryClass,
                    registryClass,
                    (left, right) -> Relation.get(java.util.Objects.equals(registry.getKey(left), registry.getKey(right)))
            );
        }
    }

    private static <R> DefaultExpression<R> defaultExpression(Class<R> registryClass) {
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
            public Class<? extends R> getReturnType() {
                return registryClass;
            }

            @Override
            public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
                return codeName(registryClass);
            }
        };
    }

    private static String codeName(Class<?> registryClass) {
        return registryClass.getSimpleName().replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ENGLISH);
    }
}
