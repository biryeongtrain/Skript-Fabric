package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Horse Domestication")
@Description({
        "Gets and/or sets the (max) domestication of a horse.",
        "The domestication of a horse is how close a horse is to becoming tame - the higher the domestication, the closer they are to becoming tame (must be between 1 and the max domestication level of the horse).",
        "The max domestication of a horse is how long it will take for a horse to become tame (must be greater than 0)."
})
@Example("""
    function domesticateAndTame(horse: entity, p: offline player, i: int = 10):
        add {_i} to domestication level of {_horse}
        if domestication level of {_horse} >= max domestication level of {_horse}:
            tame {_horse}
            set tamer of {_horse} to {_p}
    """)
@Since("2.10")
public class ExprDomestication extends SimplePropertyExpression<LivingEntity, Integer> {

    static {
        register(ExprDomestication.class, Integer.class, "[:max[imum]] domestication level", "livingentities");
    }

    private boolean max;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        max = parseResult.hasTag("max");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Integer convert(LivingEntity entity) {
        if (!(entity instanceof AbstractHorse horse)) {
            return null;
        }
        return max ? horse.getMaxTemper() : horse.getTemper();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (max) {
            return null;
        }
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int change = delta == null ? 0 : ((Number) delta[0]).intValue();
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (!(entity instanceof AbstractHorse horse)) {
                continue;
            }
            int current = horse.getTemper();
            int next = switch (mode) {
                case SET -> change;
                case ADD -> current + change;
                case REMOVE -> current - change;
                case RESET -> 1;
                default -> current;
            };
            next = Math.max(1, Math.min(next, horse.getMaxTemper()));
            horse.setTemper(next);
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return (max ? "max " : "") + "domestication level";
    }
}
