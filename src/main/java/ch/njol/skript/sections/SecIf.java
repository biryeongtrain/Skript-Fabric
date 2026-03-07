package ch.njol.skript.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SecIf extends EffectSection {

    private @Nullable Condition condition;
    private String source = "if";

    public static @Nullable SecIf parse(
            String input,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        String trimmed = input.trim();
        if (!trimmed.regionMatches(true, 0, "if ", 0, 3) || sectionNode == null) {
            return null;
        }

        Condition condition = Condition.parse(trimmed.substring(3).trim(), null);
        if (condition == null) {
            return null;
        }

        SecIf section = new SecIf();
        section.condition = condition;
        section.source = trimmed;
        section.loadCode(sectionNode);
        return section;
    }

    @Override
    public boolean init(
            ch.njol.skript.lang.Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult,
            @Nullable SectionNode sectionNode,
            @Nullable List<TriggerItem> triggerItems
    ) {
        return false;
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        return walk(event, condition != null && condition.check(event));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return source;
    }
}
