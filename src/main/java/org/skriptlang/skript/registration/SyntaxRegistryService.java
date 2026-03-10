package org.skriptlang.skript.registration;

import ch.njol.skript.lang.SyntaxElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.Structure;

public class SyntaxRegistryService {

    private final Map<String, List<SyntaxInfo<?>>> syntaxesByKey = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <I extends SyntaxInfo<?>> Iterable<I> syntaxes(String key) {
        List<SyntaxInfo<?>> syntaxes = syntaxesByKey.get(key);
        if (syntaxes == null || syntaxes.isEmpty()) {
            return Collections.emptyList();
        }
        return (Iterable<I>) Collections.unmodifiableList(syntaxes);
    }

    public <E extends SyntaxElement> void register(String key, SyntaxInfo<? extends E> info) {
        List<SyntaxInfo<?>> syntaxes = syntaxesByKey.computeIfAbsent(key, unused -> new ArrayList<>());
        for (SyntaxInfo<?> existing : syntaxes) {
            if (existing.type() == info.type() && Arrays.equals(existing.patterns(), info.patterns())) {
                return;
            }
        }
        int insertionIndex = syntaxes.size();
        for (int index = 0; index < syntaxes.size(); index++) {
            if (info.priority().compareTo(syntaxes.get(index).priority()) < 0) {
                insertionIndex = index;
                break;
            }
        }
        syntaxes.add(insertionIndex, info);
    }

    public <E extends SyntaxElement> void register(String key, Class<? extends E> syntaxClass, String... patterns) {
        @SuppressWarnings("unchecked")
        Class<E> type = (Class<E>) syntaxClass;
        register(key, new SyntaxInfo<>(type, patterns, syntaxClass.getName()));
    }

    public <E extends ch.njol.skript.lang.Expression<T>, T> void registerExpression(
            Class<? extends E> expressionClass,
            Class<T> returnType,
            String... patterns
    ) {
        @SuppressWarnings("unchecked")
        Class<E> type = (Class<E>) expressionClass;
        SyntaxInfo.Expression<E, T> info = SyntaxInfo.Expression.<E, T>builder(type, returnType)
                .patterns(patterns)
                .originClassPath(expressionClass.getName())
                .build();
        register(SyntaxRegistry.EXPRESSION, info);
    }

    public <E extends Structure> void registerStructure(
            Class<? extends E> structureClass,
            SyntaxInfo.Structure.NodeType nodeType,
            @Nullable EntryValidator entryValidator,
            String... patterns
    ) {
        @SuppressWarnings("unchecked")
        Class<E> type = (Class<E>) structureClass;
        SyntaxInfo.Structure<E> info = SyntaxInfo.Structure.<E>builder(type)
                .patterns(patterns)
                .originClassPath(structureClass.getName())
                .entryValidator(entryValidator)
                .nodeType(nodeType)
                .build();
        register(SyntaxRegistry.STRUCTURE, info);
    }

    public void clear(String key) {
        syntaxesByKey.remove(key);
    }

    public void clearAll() {
        syntaxesByKey.clear();
    }
}
