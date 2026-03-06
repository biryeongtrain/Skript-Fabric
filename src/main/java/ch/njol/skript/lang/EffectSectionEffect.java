package ch.njol.skript.lang;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffectSectionEffect extends Effect {

    private final EffectSection effectSection;

    public EffectSectionEffect(EffectSection effectSection) {
        this.effectSection = effectSection;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return effectSection.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected void execute(SkriptEvent event) {
        // Execution is delegated via walk(event).
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        return effectSection.walk(event);
    }

    @Override
    public String getIndentation() {
        return effectSection.getIndentation();
    }

    @Override
    public TriggerItem setParent(@Nullable TriggerSection parent) {
        return effectSection.setParent(parent);
    }

    @Override
    public TriggerItem setNext(@Nullable TriggerItem next) {
        return effectSection.setNext(next);
    }

    @Override
    public @Nullable TriggerItem getNext() {
        return effectSection.getNext();
    }

    @Override
    protected @Nullable ExecutionIntent executionIntent() {
        return effectSection.executionIntent();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return effectSection.toString(event, debug);
    }
}
