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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Minecart Derailed / Flying Velocity")
@Description("The configured velocity modifier of a minecart when derailed or airborne on the Fabric compatibility surface.")
@Example("""
    on right click on minecart:
        set derailed velocity of event-entity to vector(2, 10, 2)
    """)
@Since("2.5.1, 2.13 (Fabric)")
public class ExprMinecartDerailedFlyingVelocity extends SimplePropertyExpression<Entity, Vec3> {

    static {
        register(ExprMinecartDerailedFlyingVelocity.class, Vec3.class, "[minecart] (1¦derailed|2¦flying) velocity", "entities");
    }

    private boolean flying;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        flying = parseResult.mark == 2;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Vec3 convert(Entity entity) {
        return entity instanceof AbstractMinecart minecart ? MinecartExpressionSupport.velocity(minecart, flying) : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Vec3.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null) {
            return;
        }
        Vec3 input = (Vec3) delta[0];
        for (Entity entity : getExpr().getArray(event)) {
            if (!(entity instanceof AbstractMinecart minecart)) {
                continue;
            }
            Vec3 current = MinecartExpressionSupport.velocity(minecart, flying);
            Vec3 next = switch (mode) {
                case SET -> input;
                case ADD -> current.add(input);
                case REMOVE -> current.subtract(input);
                default -> current;
            };
            MinecartExpressionSupport.setVelocity(minecart, flying, next);
        }
    }

    @Override
    protected String getPropertyName() {
        return flying ? "flying velocity" : "derailed velocity";
    }

    @Override
    public Class<? extends Vec3> getReturnType() {
        return Vec3.class;
    }
}
