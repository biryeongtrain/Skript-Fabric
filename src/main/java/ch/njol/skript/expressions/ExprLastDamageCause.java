package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceTypeSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Last Damage Cause")
@Description("Cause of last damage done to an entity.")
@Example("send last damage cause of target")
@Since("2.2-Fixes-V10")
public class ExprLastDamageCause extends PropertyExpression<LivingEntity, String> {

    static {
        register(ExprLastDamageCause.class, String.class, "last damage (cause|reason|type)", "livingentities");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<LivingEntity>) expressions[0]);
        return true;
    }

    @Override
    protected String[] get(SkriptEvent event, LivingEntity[] source) {
        return get(source, entity -> entity.getLastDamageSource() == null
                ? null
                : DamageSourceTypeSupport.display(entity.getLastDamageSource()));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the damage cause " + getExpr().toString(event, debug);
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }
}
