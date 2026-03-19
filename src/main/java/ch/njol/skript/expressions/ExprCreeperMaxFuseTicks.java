package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import kim.biryeong.skriptFabric.mixin.CreeperAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Creeper Max Fuse Ticks")
@Description("The max fuse ticks that a creeper has.")
@Example("set target entity's max fuse ticks to 20 #1 second")
@Since("2.5")
public class ExprCreeperMaxFuseTicks extends SimplePropertyExpression<LivingEntity, Long> {

    static {
        register(ExprCreeperMaxFuseTicks.class, Long.class, "[creeper] max[imum] fuse tick[s]", "livingentities");
    }

    @Override
    public Long convert(LivingEntity entity) {
        if (!(entity instanceof Creeper creeper)) {
            return 0L;
        }
        return (long) ((CreeperAccessor) creeper).skript$getMaxSwell();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, SET, DELETE, RESET, REMOVE -> new Class[]{Number.class};
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int change = delta == null ? 0 : ((Number) delta[0]).intValue();
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (!(entity instanceof Creeper creeper)) {
                continue;
            }
            CreeperAccessor accessor = (CreeperAccessor) creeper;
            int next = switch (mode) {
                case ADD -> accessor.skript$getMaxSwell() + change;
                case SET -> change;
                case DELETE -> 0;
                case RESET -> 30;
                case REMOVE -> accessor.skript$getMaxSwell() - change;
            };
            accessor.skript$setMaxSwell(Math.max(0, next));
        }
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "creeper max fuse ticks";
    }
}
