package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Portal Cooldown")
@Description({
        "The amount of time before an entity can use a portal. By default, it is 15 seconds after exiting a nether portal or end gateway.",
        "Players in survival/adventure get a cooldown of 0.5 seconds, while those in creative get no cooldown.",
        "Resetting will set the cooldown back to the default 15 seconds for non-player entities and 0.5 seconds for players."
})
@Example("""
    on portal:
        wait 1 tick
        set portal cooldown of event-entity to 5 seconds
    """)
@Since("2.8.0")
public class ExprPortalCooldown extends SimplePropertyExpression<Entity, Timespan> {

    private static final int DEFAULT_COOLDOWN = 15 * 20;
    private static final int DEFAULT_COOLDOWN_PLAYER = 10;

    static {
        register(ExprPortalCooldown.class, Timespan.class, "portal cooldown", "entities");
    }

    @Override
    public Timespan convert(Entity entity) {
        return new Timespan(Timespan.TimePeriod.TICK, entity.getPortalCooldown());
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, RESET, DELETE, REMOVE -> new Class[]{Timespan.class};
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
        for (Entity entity : getExpr().getArray(event)) {
            switch (mode) {
                case REMOVE -> entity.setPortalCooldown(Math.max(entity.getPortalCooldown() - change, 0));
                case ADD -> entity.setPortalCooldown(Math.max(entity.getPortalCooldown() + change, 0));
                case RESET -> entity.setPortalCooldown(entity instanceof ServerPlayer player ? (player.isCreative() ? 0 : DEFAULT_COOLDOWN_PLAYER) : DEFAULT_COOLDOWN);
                case DELETE, SET -> entity.setPortalCooldown(Math.max(change, 0));
            }
        }
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "portal cooldown";
    }
}
