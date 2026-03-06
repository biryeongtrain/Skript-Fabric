package ch.njol.skript.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ExecutionIntent extends Comparable<ExecutionIntent>
        permits ExecutionIntent.StopTrigger, ExecutionIntent.StopSections {

    static StopTrigger stopTrigger() {
        return new StopTrigger();
    }

    static StopSections stopSections(int levels) {
        if (levels < 1) {
            throw new IllegalArgumentException("levels must be >= 1");
        }
        return new StopSections(levels);
    }

    static StopSections stopSection() {
        return new StopSections(1);
    }

    @Nullable ExecutionIntent use();

    final class StopTrigger implements ExecutionIntent {

        private StopTrigger() {
        }

        @Override
        public StopTrigger use() {
            return new StopTrigger();
        }

        @Override
        public int compareTo(@NotNull ExecutionIntent other) {
            return other instanceof StopTrigger ? 0 : 1;
        }

        @Override
        public String toString() {
            return "StopTrigger";
        }
    }

    final class StopSections implements ExecutionIntent {

        private final int levels;

        private StopSections(int levels) {
            this.levels = levels;
        }

        public int levels() {
            return levels;
        }

        @Override
        public @Nullable StopSections use() {
            return levels > 1 ? new StopSections(levels - 1) : null;
        }

        @Override
        public int compareTo(@NotNull ExecutionIntent other) {
            if (!(other instanceof StopSections stopSections)) {
                return other.compareTo(this) * -1;
            }
            return Integer.compare(levels, stopSections.levels);
        }

        @Override
        public String toString() {
            return "StopSections(levels=" + levels + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof StopSections other)) {
                return false;
            }
            return levels == other.levels;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(levels);
        }
    }
}
