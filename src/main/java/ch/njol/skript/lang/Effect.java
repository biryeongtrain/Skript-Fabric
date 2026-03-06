package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
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

        var iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT).iterator();
        @SuppressWarnings({"rawtypes", "unchecked"})
        Effect effect = (Effect) SkriptParser.parseModern(
                expression,
                (Iterator) iterator,
                ParseContext.DEFAULT,
                defaultError
        );
        return effect;
    }
}
