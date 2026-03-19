package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import kim.biryeong.skriptFabric.mixin.BiomeAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

public class ExprHumidity extends SimplePropertyExpression<FabricBlock, Number> {

    static {
        register(ExprHumidity.class, Number.class, "humidit(y|ies)", "blocks");
    }

    @Override
    public @Nullable Number convert(FabricBlock block) {
        if (block.level() == null) {
            return null;
        }
        Biome biome = block.level().getBiome(block.position()).value();
        return ((BiomeAccessor) (Object) biome).skript$getClimateSettings().downfall();
    }

    @Override
    protected String getPropertyName() {
        return "humidity";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
