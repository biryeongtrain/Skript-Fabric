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
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Applied Beacon Effect")
@Description("The effect type currently being applied by a beacon effect event.")
@Example("""
    on primary beacon effect:
        if applied effect is speed:
            broadcast "primary speed"
    """)
@Since("2.10, Fabric")
public class ExprAppliedEffect extends SimpleExpression<Holder<MobEffect>> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprAppliedEffect.class, (Class) Holder.class, "[the] applied [beacon] effect");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.BeaconEffect.class};
    }

    @Override
    protected Holder<MobEffect> @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.BeaconEffect handle)) {
            return null;
        }
        Holder<MobEffect> effect = PotionEffectSupport.parsePotionType(handle.effectType());
        return effect == null ? new Holder[0] : new Holder[]{effect};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Holder<MobEffect>> getReturnType() {
        return (Class) Holder.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "applied effect";
    }
}
