package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

public class ExprHumidity extends SimplePropertyExpression<FabricBlock, Number> {

    static {
        register(ExprHumidity.class, Number.class, "humidit(y|ies)", "blocks");
    }

    @Override
    public @Nullable Number convert(FabricBlock block) {
        return block.level() == null
                ? null
                : humidity(block.level().getBiome(block.position()).value());
    }

    @Override
    protected String getPropertyName() {
        return "humidity";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    static @Nullable Number humidity(Object biome) {
        Object climateSettings = ReflectiveHandleAccess.invokeNoArg(biome, "getModifiedClimateSettings");
        if (climateSettings == null) {
            try {
                Field field = biome.getClass().getDeclaredField("climateSettings");
                field.setAccessible(true);
                climateSettings = field.get(biome);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        if (climateSettings != null) {
            Object downfall = ReflectiveHandleAccess.invokeNoArg(climateSettings, "downfall");
            if (downfall instanceof Number number) {
                return number;
            }
            try {
                Method method = climateSettings.getClass().getDeclaredMethod("downfall");
                method.setAccessible(true);
                Object value = method.invoke(climateSettings);
                if (value instanceof Number number) {
                    return number;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }
}
