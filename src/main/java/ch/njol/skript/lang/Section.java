package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class Section extends TriggerSection implements SyntaxElement {

    static {
        ParserInstance.registerData(SectionContext.class, SectionContext::new);
    }

    private @Nullable Node node;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        node = getParser().getNode();
        SectionContext sectionContext = getParser().getData(SectionContext.class);
        return sectionContext.attemptClaim(
                this,
                parseResult.expr,
                (context, syntax) -> syntax.init(
                        expressions,
                        matchedPattern,
                        isDelayed,
                        parseResult,
                        context.sectionNode,
                        context.triggerItems
                )
        );
    }

    public abstract boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    );

    protected void loadCode(SectionNode sectionNode) {
        ParserInstance parser = getParser();
        List<TriggerSection> previousSections = parser.getCurrentSections();

        List<TriggerSection> sections = new ArrayList<>(previousSections);
        sections.add(this);
        parser.setCurrentSections(sections);

        try {
            setTriggerItems(ScriptLoader.loadItems(sectionNode));
        } finally {
            parser.setCurrentSections(previousSections);
        }
    }

    protected void loadOptionalCode(SectionNode sectionNode) {
        loadCode(sectionNode);
    }

    @SafeVarargs
    protected final Trigger loadCode(SectionNode sectionNode, String name, Class<?>... events) {
        return loadCode(sectionNode, name, null, null, events);
    }

    @SafeVarargs
    protected final Trigger loadCode(
            SectionNode sectionNode,
            String name,
            @Nullable Runnable beforeLoading,
            @Nullable Runnable afterLoading,
            Class<?>... events
    ) {
        ParserInstance parser = getParser();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        List<TriggerSection> previousSections = parser.getCurrentSections();

        parser.setCurrentEvent(name, events);
        parser.setCurrentSections(new ArrayList<>());
        if (beforeLoading != null) {
            beforeLoading.run();
        }

        List<TriggerItem> triggerItems = ScriptLoader.loadItems(sectionNode);

        if (afterLoading != null) {
            afterLoading.run();
        }

        parser.setCurrentSections(previousSections);
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }

        return new Trigger(
                parser.getCurrentScript(),
                name,
                new SectionSkriptEvent(name, this),
                triggerItems
        );
    }

    public static @Nullable Section parse(
            String expr,
            @Nullable String defaultError,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        SectionContext sectionContext = ParserInstance.get().getData(SectionContext.class);
        return sectionContext.modify(sectionNode, triggerItems, () -> {
            var iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.SECTION).iterator();
            @SuppressWarnings({"rawtypes", "unchecked"})
            Section section = (Section) SkriptParser.parseModern(
                    expr,
                    (Iterator) iterator,
                    ParseContext.DEFAULT,
                    defaultError
            );
            return section;
        });
    }

    public @Nullable Node getNode() {
        return node;
    }

    @Override
    public @NotNull String getSyntaxTypeName() {
        return "section";
    }

    public static class SectionContext extends ParserInstance.Data {

        protected @Nullable SectionNode sectionNode;
        protected @Nullable List<TriggerItem> triggerItems;
        protected @Nullable Debuggable owner;
        protected @Nullable String ownerErrorRepresentation;

        public SectionContext(ParserInstance parserInstance) {
            super(parserInstance);
        }

        public <T> T modify(
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems,
                Supplier<T> supplier
        ) {
            SectionNode previousSectionNode = this.sectionNode;
            List<TriggerItem> previousTriggerItems = this.triggerItems;
            Debuggable previousOwner = this.owner;
            String previousOwnerErrorRepresentation = this.ownerErrorRepresentation;

            this.sectionNode = sectionNode;
            this.triggerItems = triggerItems;
            this.owner = null;
            this.ownerErrorRepresentation = null;

            T result = supplier.get();

            this.sectionNode = previousSectionNode;
            this.triggerItems = previousTriggerItems;
            this.owner = previousOwner;
            this.ownerErrorRepresentation = previousOwnerErrorRepresentation;

            return result;
        }

        public <Syntax extends SyntaxElement & Debuggable> boolean attemptClaim(
                Syntax syntax,
                String errorRepresentation,
                BiFunction<SectionContext, Syntax, Boolean> runIfClaimed
        ) {
            if (!claim(syntax, errorRepresentation)) {
                return false;
            }
            boolean success = runIfClaimed.apply(this, syntax);
            if (!success) {
                unclaim(syntax);
            }
            return success;
        }

        public <Syntax extends SyntaxElement & Debuggable> boolean claim(Syntax syntax, String errorRepresentation) {
            if (sectionNode == null) {
                return true;
            }
            if (claimed()) {
                if (owner == syntax) {
                    return true;
                }
                Skript.error(
                        "The syntax '" + errorRepresentation
                                + "' tried to claim the current section, but it was already claimed by '"
                                + ownerErrorRepresentation
                                + "'. You cannot have two section-starters in the same line."
                );
                return false;
            }
            owner = syntax;
            ownerErrorRepresentation = errorRepresentation;
            return true;
        }

        public <Syntax extends SyntaxElement & Debuggable> void unclaim(Syntax syntax) {
            if (sectionNode == null) {
                return;
            }
            if (!claimed() || owner != syntax) {
                throw new IllegalStateException("Tried to unclaim a section that is not owned by this syntax.");
            }
            owner = null;
            ownerErrorRepresentation = null;
        }

        public boolean claimed() {
            return owner != null;
        }
    }
}
