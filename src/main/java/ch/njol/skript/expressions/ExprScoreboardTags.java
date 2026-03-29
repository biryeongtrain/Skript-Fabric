package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Scoreboard Tags")
@Description("A flattened list of the scoreboard tags stored on the given entities.")
@Example("add \"quest-npc\" to scoreboard tags of event-entity")
@Since("2.3")
public class ExprScoreboardTags extends SimpleExpression<String> {

    static {
        Skript.registerExpression(
                ExprScoreboardTags.class,
                String.class,
                "[(all [[of] the]|the)] scoreboard tags of %entities%",
                "%entities%'[s] scoreboard tags"
        );
    }

    private Expression<Entity> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        List<String> values = new ArrayList<>();
        for (Entity entity : entities.getArray(event)) {
            values.addAll(entity.entityTags());
        }
        return values.toArray(String[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{String[].class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (Entity entity : entities.getArray(event)) {
            if (mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
                clearTags(entity);
            }
            if ((mode == ChangeMode.SET || mode == ChangeMode.ADD) && delta != null) {
                for (Object value : delta) {
                    entity.addTag((String) value);
                }
            } else if (mode == ChangeMode.REMOVE && delta != null) {
                for (Object value : delta) {
                    entity.removeTag((String) value);
                }
            }
        }
    }

    private static void clearTags(Entity entity) {
        Set<String> existing = Set.copyOf(entity.entityTags());
        for (String tag : existing) {
            entity.removeTag(tag);
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "scoreboard tags of " + entities.toString(event, debug);
    }
}
