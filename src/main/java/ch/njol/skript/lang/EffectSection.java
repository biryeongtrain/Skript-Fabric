package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class EffectSection extends Section {

    static {
        ParserInstance.registerData(EffectSectionContext.class, EffectSectionContext::new);
    }

    private boolean hasSection;

    public boolean hasSection() {
        return hasSection;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        ParserInstance parser = getParser();
        SectionContext sectionContext = parser.getData(SectionContext.class);
        EffectSectionContext effectSectionContext = parser.getData(EffectSectionContext.class);
        SectionNode sectionNode = sectionContext.sectionNode;
        if (!effectSectionContext.isNodeForEffectSection) {
            sectionContext.sectionNode = null;
        }

        hasSection = sectionContext.sectionNode != null;
        boolean result = super.init(expressions, matchedPattern, isDelayed, parseResult);

        if (!effectSectionContext.isNodeForEffectSection) {
            sectionContext.sectionNode = sectionNode;
        }
        return result;
    }

    @Override
    public abstract boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    );

    public static @Nullable EffectSection parse(
            String input,
            @Nullable String defaultError,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        return parse(input, defaultError, sectionNode, true, triggerItems);
    }

    public static @Nullable EffectSection parse(
            String input,
            @Nullable String defaultError,
            @Nullable SectionNode sectionNode,
            boolean isNodeForEffectSection,
            @Nullable List<TriggerItem> triggerItems
    ) {
        ParserInstance parser = ParserInstance.get();
        SectionContext sectionContext = parser.getData(SectionContext.class);
        EffectSectionContext effectSectionContext = parser.getData(EffectSectionContext.class);
        boolean wasNodeForEffectSection = effectSectionContext.isNodeForEffectSection;
        effectSectionContext.isNodeForEffectSection = isNodeForEffectSection;

        try {
            return sectionContext.modify(sectionNode, triggerItems, () -> {
                List<SyntaxInfo<?>> effectSectionInfos = new ArrayList<>();
                for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.SECTION)) {
                    if (EffectSection.class.isAssignableFrom(info.type())) {
                        effectSectionInfos.add(info);
                    }
                }

                @SuppressWarnings({"rawtypes", "unchecked"})
                EffectSection parsed = (EffectSection) SkriptParser.parseModern(
                        input,
                        (Iterator) effectSectionInfos.iterator(),
                        ParseContext.DEFAULT,
                        defaultError
                );
                if (parsed != null && sectionNode != null && !sectionContext.claimed()) {
                    Skript.error(
                            "The line '" + input + "' is a valid statement but cannot function as a section (:) "
                                    + "because there is no syntax in the line to manage it."
                    );
                    return null;
                }
                return parsed;
            });
        } finally {
            effectSectionContext.isNodeForEffectSection = wasNodeForEffectSection;
        }
    }

    private static class EffectSectionContext extends ParserInstance.Data {

        private boolean isNodeForEffectSection = true;

        public EffectSectionContext(ParserInstance parserInstance) {
            super(parserInstance);
        }
    }
}
