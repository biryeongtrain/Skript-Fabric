package org.skriptlang.skript.registration;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SyntaxElement;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.util.Priority;

public class SyntaxInfo<E extends SyntaxElement> {

    public static final Priority SIMPLE = Priority.base();
    public static final Priority COMBINED = Priority.after(SIMPLE);
    public static final Priority PATTERN_MATCHES_EVERYTHING = Priority.after(COMBINED);

    private final Class<E> type;
    private final String[] patterns;
    private final String originClassPath;
    private final Priority priority;

    public SyntaxInfo(Class<E> type, String[] patterns, String originClassPath) {
        this(type, patterns, originClassPath, COMBINED);
    }

    public SyntaxInfo(Class<E> type, String[] patterns, String originClassPath, Priority priority) {
        this.type = type;
        this.patterns = patterns;
        this.originClassPath = originClassPath;
        this.priority = priority;
    }

    public Class<E> type() {
        return type;
    }

    public String[] patterns() {
        return patterns;
    }

    public String originClassPath() {
        return originClassPath;
    }

    public Priority priority() {
        return priority;
    }

    public static class Structure<E extends org.skriptlang.skript.lang.structure.Structure> extends SyntaxInfo<E> {

        public enum NodeType {
            SIMPLE(true, false),
            SECTION(false, true),
            BOTH(true, true);

            private final boolean simple;
            private final boolean section;

            NodeType(boolean simple, boolean section) {
                this.simple = simple;
                this.section = section;
            }

            public boolean canBeSimple() {
                return simple;
            }

            public boolean canBeSection() {
                return section;
            }
        }

        private final @Nullable EntryValidator entryValidator;
        private final NodeType nodeType;

        public Structure(
                Class<E> type,
                String[] patterns,
                String originClassPath,
                @Nullable EntryValidator entryValidator,
                NodeType nodeType
        ) {
            this(type, patterns, originClassPath, entryValidator, nodeType, COMBINED);
        }

        public Structure(
                Class<E> type,
                String[] patterns,
                String originClassPath,
                @Nullable EntryValidator entryValidator,
                NodeType nodeType,
                Priority priority
        ) {
            super(type, patterns, originClassPath, priority);
            this.entryValidator = entryValidator;
            this.nodeType = nodeType;
        }

        public @Nullable EntryValidator entryValidator() {
            return entryValidator;
        }

        public NodeType nodeType() {
            return nodeType;
        }

        public static <E extends org.skriptlang.skript.lang.structure.Structure> Builder<E> builder(Class<E> type) {
            return new Builder<>(type);
        }

        public static class Builder<E extends org.skriptlang.skript.lang.structure.Structure> {

            private final Class<E> type;
            private String[] patterns = new String[0];
            private String originClassPath = "";
            private @Nullable EntryValidator entryValidator;
            private NodeType nodeType = NodeType.SECTION;
            private Priority priority = COMBINED;

            public Builder(Class<E> type) {
                this.type = type;
            }

            public Builder<E> patterns(String... patterns) {
                this.patterns = patterns;
                return this;
            }

            public Builder<E> originClassPath(String path) {
                this.originClassPath = path;
                return this;
            }

            public Builder<E> entryValidator(@Nullable EntryValidator validator) {
                this.entryValidator = validator;
                return this;
            }

            public Builder<E> nodeType(NodeType nodeType) {
                this.nodeType = nodeType;
                return this;
            }

            public Builder<E> priority(Priority priority) {
                this.priority = priority;
                return this;
            }

            public Structure<E> build() {
                return new Structure<>(type, patterns, originClassPath, entryValidator, nodeType, priority);
            }
        }
    }

    public static class Expression<E extends ch.njol.skript.lang.Expression<T>, T> extends SyntaxInfo<E> {

        private final Class<T> returnType;

        public Expression(
                Class<E> type,
                String[] patterns,
                String originClassPath,
                Class<T> returnType
        ) {
            this(type, patterns, originClassPath, returnType, COMBINED);
        }

        public Expression(
                Class<E> type,
                String[] patterns,
                String originClassPath,
                Class<T> returnType,
                Priority priority
        ) {
            super(type, patterns, originClassPath, priority);
            this.returnType = returnType;
        }

        public Class<T> returnType() {
            return returnType;
        }

        public static <E extends ch.njol.skript.lang.Expression<T>, T> Builder<E, T> builder(Class<E> type, Class<T> returnType) {
            return new Builder<>(type, returnType);
        }

        public static class Builder<E extends ch.njol.skript.lang.Expression<T>, T> {

            private final Class<E> type;
            private final Class<T> returnType;
            private String[] patterns = new String[0];
            private String originClassPath = "";
            private Priority priority = COMBINED;

            public Builder(Class<E> type, Class<T> returnType) {
                this.type = type;
                this.returnType = returnType;
            }

            public Builder<E, T> patterns(String... patterns) {
                this.patterns = patterns;
                return this;
            }

            public Builder<E, T> originClassPath(String path) {
                this.originClassPath = path;
                return this;
            }

            public Builder<E, T> priority(Priority priority) {
                this.priority = priority;
                return this;
            }

            public Expression<E, T> build() {
                return new Expression<>(type, patterns, originClassPath, returnType, priority);
            }
        }
    }
}
