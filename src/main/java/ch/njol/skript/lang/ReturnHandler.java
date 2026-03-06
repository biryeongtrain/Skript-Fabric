package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.Deque;
import java.util.LinkedList;
import org.jetbrains.annotations.Nullable;

public interface ReturnHandler<T> {

    default void loadReturnableSectionCode(SectionNode node) {
        if (!(this instanceof Section section)) {
            throw new SkriptAPIException("loadReturnableSectionCode called on a non-section object");
        }
        ParserInstance parser = ParserInstance.get();
        ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);
        stack.push(this);
        try {
            section.loadCode(node);
        } finally {
            stack.pop();
        }
    }

    default ReturnableTrigger<T> loadReturnableSectionCode(SectionNode node, String name, Class<?>[] events) {
        if (!(this instanceof Section section)) {
            throw new SkriptAPIException("loadReturnableSectionCode called on a non-section object");
        }
        ParserInstance parser = ParserInstance.get();
        ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);
        try {
            return new ReturnableTrigger<>(
                    this,
                    parser.getCurrentScript(),
                    name,
                    new SectionSkriptEvent(name, section),
                    trigger -> {
                        stack.push(trigger);
                        return ScriptLoader.loadItems(node);
                    }
            );
        } finally {
            stack.pop();
        }
    }

    default ReturnableTrigger<T> loadReturnableTrigger(SectionNode node, String name, ch.njol.skript.lang.SkriptEvent event) {
        ParserInstance parser = ParserInstance.get();
        ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);
        try {
            return new ReturnableTrigger<>(
                    this,
                    parser.getCurrentScript(),
                    name,
                    event,
                    trigger -> {
                        stack.push(trigger);
                        return ScriptLoader.loadItems(node);
                    }
            );
        } finally {
            stack.pop();
        }
    }

    void returnValues(org.skriptlang.skript.lang.event.SkriptEvent event, Expression<? extends T> value);

    boolean isSingleReturnValue();

    @Nullable Class<? extends T> returnValueType();

    final class ReturnHandlerStack extends ParserInstance.Data {

        private final Deque<ReturnHandler<?>> stack = new LinkedList<>();

        public ReturnHandlerStack(ParserInstance parserInstance) {
            super(parserInstance);
        }

        public Deque<ReturnHandler<?>> getStack() {
            return stack;
        }

        public @Nullable ReturnHandler<?> getCurrentHandler() {
            return stack.peek();
        }

        public void push(ReturnHandler<?> handler) {
            stack.push(handler);
        }

        public ReturnHandler<?> pop() {
            return stack.pop();
        }
    }

}
