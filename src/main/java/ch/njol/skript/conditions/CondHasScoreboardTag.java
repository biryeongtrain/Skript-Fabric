package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import net.minecraft.world.entity.Entity;

@Name("Has Scoreboard Tag")
@Description("Checks whether the given entities has the given <a href='#ExprScoreboardTags'>scoreboard tags</a>.")
@Example("if the targeted armor stand has the scoreboard tag \"test tag\":")
@Since("2.3")
public class CondHasScoreboardTag extends Condition {

    static {
        PropertyCondition.register(CondHasScoreboardTag.class, PropertyType.HAVE, "[the] score[ ]board tag[s] %strings%", "entities");
    }

    private Expression<Entity> entities;
    private Expression<String> tags;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        tags = (Expression<String>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        String[] tagsList = tags.getAll(event);
        return entities.check(
                event,
                entity -> {
                    if (tags.getAnd()) {
                        for (String tag : tagsList) {
                            if (!entity.getTags().contains(tag)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    for (String tag : tagsList) {
                        if (entity.getTags().contains(tag)) {
                            return true;
                        }
                    }
                    return false;
                },
                isNegated()
        );
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(
                this,
                PropertyType.HAVE,
                event,
                debug,
                entities,
                "the scoreboard " + (tags.isSingle() ? "tag " : "tags ") + tags.toString(event, debug)
        );
    }
}
