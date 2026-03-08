package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class Effect extends Statement {

    protected abstract void execute(SkriptEvent event);

    @Override
    protected final boolean run(SkriptEvent event) {
        execute(event);
        return true;
    }

    public static @Nullable Effect parse(String input, @Nullable String defaultError) {
        return parse(input, defaultError, null, null);
    }

    public static @Nullable Effect parse(
            String input,
            @Nullable String defaultError,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String expression = input.trim();

        EffectSection section = EffectSection.parse(expression, defaultError, sectionNode, triggerItems);
        if (section != null) {
            return new EffectSectionEffect(section);
        }

        Section.SectionContext sectionContext = ParserInstance.get().getData(Section.SectionContext.class);
        if (sectionNode != null) {
            return sectionContext.modify(sectionNode, triggerItems, () -> {
                Effect parsed = parseRegisteredEffect(expression, defaultError, sectionContext);
                if (parsed != null && !sectionContext.claimed()) {
                    Skript.error("The line '" + expression
                            + "' is a valid effect but cannot function as a section (:) because there is no syntax in the line to manage it.");
                    return null;
                }
                return parsed;
            });
        }

        return parseRegisteredEffect(expression, defaultError, null);
    }

    private static @Nullable Effect parseRegisteredEffect(
            String expression,
            @Nullable String defaultError,
            @Nullable Section.SectionContext sectionContext
    ) {
        var iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT).iterator();
        Iterator<?> parseIterator = iterator;
        if (sectionContext != null) {
            Debuggable baselineOwner = sectionContext.owner;
            String baselineOwnerErrorRepresentation = sectionContext.ownerErrorRepresentation;
            parseIterator = new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Object next() {
                    sectionContext.owner = baselineOwner;
                    sectionContext.ownerErrorRepresentation = baselineOwnerErrorRepresentation;
                    return iterator.next();
                }
            };
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        Effect effect = (Effect) SkriptParser.parseModern(
                expression,
                (Iterator) parseIterator,
                ParseContext.DEFAULT,
                defaultError
        );
        return effect;
    }
}
