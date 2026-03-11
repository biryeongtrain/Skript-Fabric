package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Shooter")
@Description("The shooter of a projectile.")
@Example("shooter is a skeleton")
@Since("1.3.7, 2.11 (entity shoot bow event)")
public class ExprShooter extends SimpleExpression<LivingEntity> {

    static {
        Skript.registerExpression(ExprShooter.class, LivingEntity.class, "[the] shooter [of %-projectiles%]");
    }

    private @Nullable Expression<Projectile> projectiles;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        projectiles = expressions[0] == null ? null : (Expression<Projectile>) expressions[0];
        return projectiles != null || getParser().isCurrentEvent(FabricEventCompatHandles.EntityShootBow.class);
    }

    @Override
    protected LivingEntity @Nullable [] get(SkriptEvent event) {
        if (projectiles == null) {
            if (!(event.handle() instanceof FabricEventCompatHandles.EntityShootBow handle)) {
                return null;
            }
            return new LivingEntity[]{handle.entity()};
        }
        return projectiles.stream(event)
                .map(Projectile::getOwner)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .toArray(LivingEntity[]::new);
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{LivingEntity.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || projectiles == null) {
            return;
        }
        Entity shooter = (Entity) delta[0];
        for (Projectile projectile : projectiles.getArray(event)) {
            projectile.setOwner(shooter);
        }
    }

    @Override
    public boolean isSingle() {
        return projectiles == null || projectiles.isSingle();
    }

    @Override
    public Class<? extends LivingEntity> getReturnType() {
        return LivingEntity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return projectiles == null ? "shooter" : "shooter of " + projectiles.toString(event, debug);
    }
}
