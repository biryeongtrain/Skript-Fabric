package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

public class ExprTemperature extends SimplePropertyExpression<FabricBlock, Number> {

    static {
        register(ExprTemperature.class, Number.class, "temperature[s]", "blocks");
    }

    @Override
    public @Nullable Number convert(FabricBlock block) {
        return block.level() == null
                ? null
                : temperature(block.level().getBiome(block.position()).value());
    }

    static @Nullable Number temperature(Object biome) {
        Object climateSettings = ReflectiveHandleAccess.invokeNoArg(biome, "getModifiedClimateSettings");
        if (climateSettings == null) {
            climateSettings = ReflectiveHandleAccess.invokeNoArg(biome, "climateSettings");
        }
        if (climateSettings != null) {
            Object value = ReflectiveHandleAccess.invokeNoArg(climateSettings, "temperature", "baseTemperature");
            if (value instanceof Number number) {
                return number;
            }
        }
        Object direct = ReflectiveHandleAccess.invokeNoArg(biome, "getBaseTemperature", "baseTemperature", "temperature");
        return direct instanceof Number number ? number : null;
    }

    @Override
    protected String getPropertyName() {
        return "temperature";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
