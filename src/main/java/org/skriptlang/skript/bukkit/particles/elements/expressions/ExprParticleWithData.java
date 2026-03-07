package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprParticleWithData extends SimpleExpression<ParticleEffect> {

    private @Nullable Expression<Number> count;
    private @Nullable Expression<?> first;
    private @Nullable Expression<Number> second;
    private int pattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        count = expressions.length > 0 ? (Expression<Number>) expressions[0] : null;
        first = expressions.length > 1 ? expressions[1] : null;
        second = expressions.length > 2 ? (Expression<Number>) expressions[2] : null;
        return true;
    }

    @Override
    protected ParticleEffect @Nullable [] get(SkriptEvent event) {
        ParticleOptions options;
        if (pattern == 0) {
            ch.njol.skript.util.ColorRGB color = ch.njol.skript.util.ColorRGB.parse(first != null ? first.getSingle(event) : null);
            if (color == null) {
                color = new ch.njol.skript.util.ColorRGB(255, 255, 255);
            }
            float scale = second != null && second.getSingle(event) != null ? Math.max(0.01F, second.getSingle(event).floatValue()) : 1.0F;
            options = new DustParticleOptions(color.rgb(), scale);
        } else {
            FabricItemType itemType = first != null ? Classes.parse(String.valueOf(first.getSingle(event)), FabricItemType.class, ParseContext.DEFAULT) : null;
            ItemStack stack = itemType != null ? itemType.toStack() : new ItemStack(net.minecraft.world.item.Items.STONE);
            options = new ItemParticleOption(ParticleTypes.ITEM, stack);
        }
        ParticleEffect effect = ParticleEffect.of(options);
        if (count != null) {
            Number amount = count.getSingle(event);
            if (amount != null) {
                effect.count(Math.max(0, Math.min(16384, amount.intValue())));
            }
        }
        return new ParticleEffect[]{effect};
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends ParticleEffect> getReturnType() { return ParticleEffect.class; }
    @Override public String toString(@Nullable SkriptEvent event, boolean debug) { return pattern == 0 ? "dust particle" : "item particle"; }
}
