package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Affected Entities")
@Description("The affected entities in an area cloud effect event.")
@Example("""
    on area cloud effect:
        loop affected entities:
            broadcast "%loop-entity%"
    """)
@Since("2.4")
public class ExprAffectedEntities extends SimpleExpression<LivingEntity> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprAffectedEntities.class, LivingEntity.class, "[the] affected entities");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.AreaEffectCloudApply.class};
    }

    @Override
    protected LivingEntity @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.AreaEffectCloudApply handle)
                || handle.affectedEntities() == null) {
            return null;
        }
        return handle.affectedEntities().toArray(LivingEntity[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public Class<? extends LivingEntity> getReturnType() {
        return LivingEntity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the affected entities";
    }
}
