package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Force Attack")
@Description({
        "Makes a living entity attack an entity with a melee attack.",
        "Using 'attack' will make the attacker use the item in their main hand "
                + "and will apply extra data from the item, including enchantments and attributes.",
        "Using 'damage' with a number of hearts will not account for the item in the main hand "
                + "and will always be the number provided."
})
@Example("""
        spawn a wolf at location(0, 0, 0)
        make last spawned wolf attack all players
        """)
@Example("""
        spawn a zombie at location(0, 0, 0)
        make player damage last spawned zombie by 2
        """)
@Since("2.5.1, 2.13 (multiple, amount)")
@RequiredPlugins("Minecraft 1.15.2+")
public class EffForceAttack extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> attackers;
    private Expression<Entity> victims;
    private @Nullable Expression<Number> amount;
    private Node node;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffForceAttack.class,
                "make %livingentities% attack %entities%",
                "force %livingentities% to attack %entities%",
                "make %livingentities% damage %entities% by %number% [heart[s]]",
                "force %livingentities% to damage %entities% by %number% [heart[s]]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        attackers = (Expression<LivingEntity>) exprs[0];
        victims = (Expression<Entity>) exprs[1];
        if (matchedPattern >= 2) {
            amount = (Expression<Number>) exprs[2];
        }
        node = getParser().getNode();
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Double resolvedAmount = null;
        if (amount != null) {
            Number number = amount.getSingle(event);
            if (number == null) {
                return;
            }
            double hearts = number.doubleValue();
            if (hearts <= 0) {
                Skript.error("Cannot damage an entity by 0 or less. Consider healing instead.");
                return;
            }
            if (!Double.isFinite(hearts)) {
                return;
            }
            resolvedAmount = hearts * 2.0D;
        }

        LivingEntity[] attackingEntities = attackers.getArray(event);
        Entity[] victimEntities = victims.getArray(event);
        if (resolvedAmount == null) {
            for (Entity victim : victimEntities) {
                for (LivingEntity attacker : attackingEntities) {
                    EffectRuntimeSupport.invokeCompatible(attacker, new String[]{"attack", "doHurtTarget"}, victim);
                }
            }
            return;
        }

        float damage = resolvedAmount.floatValue();
        for (Entity victim : victimEntities) {
            for (LivingEntity attacker : attackingEntities) {
                Object damageSources = EffectRuntimeSupport.invokeCompatible(attacker, "damageSources");
                Object damageSource = damageSources == null ? null
                        : EffectRuntimeSupport.invokeCompatible(damageSources, new String[]{"mobAttack", "playerAttack"}, attacker);
                Object level = EffectRuntimeSupport.invokeCompatible(victim, "level");
                if (damageSource != null) {
                    Object invoked = level == null ? null
                            : EffectRuntimeSupport.invokeCompatible(victim, "hurtServer", level, damageSource, damage);
                    if (invoked == null) {
                        EffectRuntimeSupport.invokeCompatible(victim, "hurt", damageSource, damage);
                    }
                }
            }
        }
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("make", attackers);
        if (amount == null) {
            builder.append("attack", victims);
        } else {
            builder.append("damage", victims, "by", amount);
        }
        return builder.toString();
    }
}
