package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Damage/Heal/Repair")
@Description({
        "Damage, heal, or repair an entity or item.",
        "Servers running Spigot 1.20.4+ can optionally choose to specify a fake damage cause."
})
@Example("damage player by 5 hearts")
@Example("damage player by 3 hearts with fake cause fall")
@Example("heal the player")
@Example("repair tool of player")
@Since("1.0, 2.10 (damage cause)")
@RequiredPlugins("Spigot 1.20.4+ (for damage cause)")
public class EffHealth extends Effect {

    private enum EffectType {
        DAMAGE,
        HEAL,
        REPAIR
    }

    private static boolean registered;
    private static final Patterns<EffectType> PATTERNS = new Patterns<>(new Object[][]{
            {"damage %livingentities/itemtypes/slots% by %number% [heart[s]]", EffectType.DAMAGE},
            {"damage %livingentities% by %number% [heart[s]] with [fake] [damage] cause %damagecause%", EffectType.DAMAGE},
            {"damage %livingentities% by %number% [heart[s]] (using|with) %damagesource% [as the source]", EffectType.DAMAGE},
            {"heal %livingentities% [by %-number% [heart[s]]]", EffectType.HEAL},
            {"repair %itemtypes/slots% [by %-number%]", EffectType.REPAIR}
    });

    private Expression<?> damageables;
    private @Nullable Expression<Number> amount;
    private EffectType effectType;
    private @Nullable Expression<?> damageCause;
    private @Nullable Expression<?> damageSource;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffHealth.class, PATTERNS.getPatterns());
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        effectType = PATTERNS.getInfo(matchedPattern);
        damageables = exprs[0];
        amount = (Expression<Number>) exprs[1];
        if (matchedPattern == 1) {
            damageCause = exprs[2];
        } else if (matchedPattern == 2) {
            damageSource = exprs[2];
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Number resolved = amount == null ? null : amount.getSingle(event);
        float magnitude = resolved == null ? 0.0F : resolved.floatValue();
        for (Object value : damageables.getArray(event)) {
            if (value instanceof LivingEntity entity) {
                switch (effectType) {
                    case DAMAGE -> entity.hurt(entity.damageSources().generic(), magnitude);
                    case HEAL -> entity.heal(resolved == null ? entity.getMaxHealth() : magnitude);
                    case REPAIR -> {
                    }
                }
            } else if (value instanceof Slot slot && (effectType == EffectType.DAMAGE || effectType == EffectType.REPAIR)) {
                repairOrDamage(slot, resolved, effectType == EffectType.DAMAGE);
            } else if (value instanceof FabricItemType) {
                // ItemType mutation needs broader change-in-place wiring before this can be runtime-eligible.
            }
        }
    }

    private void repairOrDamage(Slot slot, @Nullable Number amount, boolean damage) {
        if (!slot.hasItem()) {
            return;
        }
        var stack = slot.getItem().copy();
        if (!stack.isDamageableItem()) {
            return;
        }
        int delta = amount == null ? (damage ? stack.getMaxDamage() : stack.getDamageValue()) : amount.intValue();
        int next = damage ? stack.getDamageValue() + delta : stack.getDamageValue() - delta;
        stack.setDamageValue(Math.max(0, Math.min(stack.getMaxDamage(), next)));
        slot.set(stack);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        switch (effectType) {
            case DAMAGE -> {
                builder.append("damage", damageables, "by", amount);
                if (damageCause != null) {
                    builder.append("with fake damage cause", damageCause);
                } else if (damageSource != null) {
                    builder.append("using", damageSource);
                }
            }
            case HEAL -> {
                builder.append("heal", damageables);
                if (amount != null) {
                    builder.append("by", amount);
                }
            }
            case REPAIR -> {
                builder.append("repair", damageables);
                if (amount != null) {
                    builder.append("by", amount);
                }
            }
        }
        return builder.toString();
    }
}
