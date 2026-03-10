package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprHanging extends SimpleExpression<Entity> {

    private static final @Nullable Class<?> HANGING_BREAK_EVENT =
            ExpressionHandleSupport.resolveClass("ch.njol.skript.effects.FabricEffectEventHandles$HangingBreak");
    private static final @Nullable Class<?> HANGING_PLACE_EVENT =
            ExpressionHandleSupport.resolveClass("ch.njol.skript.effects.FabricEffectEventHandles$HangingPlace");

    static {
        Skript.registerExpression(ExprHanging.class, Entity.class, "[the] hanging (entity|:remover)");
    }

    private boolean remover;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        remover = parseResult.hasTag("remover");
        if (remover && (HANGING_BREAK_EVENT == null || !getParser().isCurrentEvent(HANGING_BREAK_EVENT))) {
            Skript.error("The expression 'hanging remover' can only be used in break event");
            return false;
        }
        if (!remover) {
            if ((HANGING_BREAK_EVENT == null || HANGING_PLACE_EVENT == null)
                    || !getParser().isCurrentEvent(HANGING_BREAK_EVENT, HANGING_PLACE_EVENT)) {
                Skript.error("The expression 'hanging entity' can only be used in break and place events");
                return false;
            }
        }
        return true;
    }

    @Override
    protected Entity @Nullable [] get(SkriptEvent event) {
        Object value = ExpressionHandleSupport.invoke(event.handle(), remover ? "remover" : "entity");
        return value instanceof Entity entity ? new Entity[]{entity} : null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "hanging " + (remover ? "remover" : "entity");
    }
}
