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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.ScriptWarning;
import net.minecraft.world.entity.LivingEntity;

@Name("No Damage Ticks")
@Description("The number of ticks that an entity is invulnerable to damage for.")
@Example("""
	on damage:
		set victim's invulnerability ticks to 20 #Victim will not take damage for the next second
	""")
@Since("2.5, 2.11 (deprecated)")
@Deprecated(since = "2.11.0", forRemoval = true)
public class ExprNoDamageTicks extends SimplePropertyExpression<LivingEntity, Long> {

    static {
        registerDefault(ExprNoDamageTicks.class, Long.class, "(invulnerability|invincibility|no damage) tick[s]", "livingentities");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        ScriptWarning.printDeprecationWarning("This expression is deprecated. Please use 'invulnerability time' instead of 'invulnerability ticks'.");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public Long convert(LivingEntity entity) {
        return (long) entity.invulnerableTime;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int provided = delta != null && delta[0] instanceof Number number ? number.intValue() : 0;
        for (LivingEntity entity : getExpr().getArray(event)) {
            switch (mode) {
                case SET, DELETE, RESET -> entity.invulnerableTime = Math.max(0, provided);
                case ADD -> entity.invulnerableTime = Math.max(0, entity.invulnerableTime + provided);
                case REMOVE -> entity.invulnerableTime = Math.max(0, entity.invulnerableTime - provided);
            }
        }
    }

    @Override
    protected String getPropertyName() {
        return "no damage ticks";
    }

    @Override
    public Class<Long> getReturnType() {
        return Long.class;
    }
}
