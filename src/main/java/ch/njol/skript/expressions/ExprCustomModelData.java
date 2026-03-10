package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

@Name("Custom Model Data")
@Description({
    "Get/set the custom model data of an item. Using just `custom model data` will return an integer. Items without model data will return 0.",
    "Since 1.21.4, custom model data instead consists of a list of numbers (floats), a list of booleans (flags), a list of strings, and a list of colours. " +
            "Accessing and modifying these lists can be done type-by-type, or all at once with `complete custom model data`. " +
            "This is the more accurate and recommended method of using custom model data."
})
@Example("""
	set custom model data of player's tool to 3
	set {_model} to custom model data of player's tool
	""")
@Example("""
	set custom model data colours of {_flag} to red, white, and blue
	add 10.5 to the model data floats of {_flag}
	""")
@Example("""
	set the full custom model data of {_item} to 10, "sword", and rgb(100, 200, 30)
	""")
@RequiredPlugins("Minecraft 1.21.4+ (floats/flags/strings/colours/full model data)")
@Since({"2.5", "2.12 (floats/flags/strings/colours/full model data)"})
public class ExprCustomModelData extends PropertyExpression<ItemStack, Object> {

    static {
        List<String> patterns = new ArrayList<>();
        patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("[custom] model data", "itemstacks")));
        patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("[custom] model data (1:floats|2:flags|3:strings|4:colo[u]rs)", "itemstacks")));
        patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("(5:(complete|full)) [custom] model data", "itemstacks")));
        Skript.registerExpression(ExprCustomModelData.class, Object.class, patterns.toArray(String[]::new));
    }

    private enum CMDType {
        SINGLE_INT(Integer.class),
        FLOATS(Float.class),
        FLAGS(Boolean.class),
        STRINGS(String.class),
        COLORS(ColorRGB.class),
        ALL(Float.class, Boolean.class, String.class, ColorRGB.class);

        private final Class<?>[] types;

        CMDType(Class<?>... types) {
            this.types = types;
        }
    }

    private CMDType dataType;
    private Class<?> returnType;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        dataType = CMDType.values()[parseResult.mark];
        returnType = switch (dataType) {
            case SINGLE_INT -> Integer.class;
            case FLOATS -> Float.class;
            case FLAGS -> Boolean.class;
            case STRINGS -> String.class;
            case COLORS -> ColorRGB.class;
            case ALL -> Object.class;
        };
        setExpr((Expression<? extends ItemStack>) expressions[0]);
        return true;
    }

    @Override
    protected Object[] get(SkriptEvent event, ItemStack[] source) {
        for (ItemStack item : source) {
            CustomModelData component = item.get(DataComponents.CUSTOM_MODEL_DATA);
            if (dataType == CMDType.SINGLE_INT) {
                int value = component == null || component.floats().isEmpty() ? 0 : component.floats().getFirst().intValue();
                return new Integer[]{value};
            }
            if (component == null) {
                return empty();
            }
            return switch (dataType) {
                case SINGLE_INT -> throw new IllegalStateException("Unexpected single-int branch");
                case FLOATS -> component.floats().toArray(Float[]::new);
                case FLAGS -> component.flags().toArray(Boolean[]::new);
                case STRINGS -> component.strings().toArray(String[]::new);
                case COLORS -> component.colors().stream().map(ColorRGB::fromRgb).toArray(ColorRGB[]::new);
                case ALL -> {
                    List<Object> values = new ArrayList<>();
                    values.addAll(component.floats());
                    values.addAll(component.flags());
                    values.addAll(component.strings());
                    values.addAll(component.colors().stream().map(ColorRGB::fromRgb).toList());
                    yield values.toArray(Object[]::new);
                }
            };
        }
        return empty();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, DELETE, RESET -> Arrays.stream(dataType.types).map(Class::arrayType).toArray(Class[]::new);
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (ItemStack item : getExpr().getArray(event)) {
            CustomModelData current = item.get(DataComponents.CUSTOM_MODEL_DATA);
            if (dataType == CMDType.SINGLE_INT) {
                int value = delta == null || delta.length == 0 ? 0 : ((Number) delta[0]).intValue();
                float next = switch (mode) {
                    case ADD -> (current == null || current.floats().isEmpty() ? 0 : current.floats().getFirst()) + value;
                    case REMOVE -> (current == null || current.floats().isEmpty() ? 0 : current.floats().getFirst()) - value;
                    case SET -> value;
                    case DELETE, RESET -> 0;
                };
                item.set(DataComponents.CUSTOM_MODEL_DATA,
                        next == 0 ? null : new CustomModelData(List.of(next), List.of(), List.of(), List.of()));
                continue;
            }

            List<Float> floats = current == null ? new ArrayList<>() : new ArrayList<>(current.floats());
            List<Boolean> flags = current == null ? new ArrayList<>() : new ArrayList<>(current.flags());
            List<String> strings = current == null ? new ArrayList<>() : new ArrayList<>(current.strings());
            List<Integer> colors = current == null ? new ArrayList<>() : new ArrayList<>(current.colors());

            if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
                floats.clear();
                flags.clear();
                strings.clear();
                colors.clear();
            } else if (delta != null) {
                if (mode == ChangeMode.SET && dataType != CMDType.ALL) {
                    clearType(floats, flags, strings, colors);
                } else if (mode == ChangeMode.SET) {
                    floats.clear();
                    flags.clear();
                    strings.clear();
                    colors.clear();
                }
                for (Object value : delta) {
                    if (value instanceof Float aFloat) {
                        apply(floats, aFloat, mode);
                    } else if (value instanceof Boolean aBoolean) {
                        apply(flags, aBoolean, mode);
                    } else if (value instanceof String string) {
                        apply(strings, string, mode);
                    } else if (value instanceof ColorRGB color) {
                        apply(colors, color.rgb(), mode);
                    }
                }
            }

            item.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
        }
    }

    @Override
    public boolean isSingle() {
        return dataType == CMDType.SINGLE_INT && getExpr().isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return dataType.types;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (dataType) {
            case ALL -> "complete custom model data";
            case FLOATS -> "custom model data floats";
            case FLAGS -> "custom model data flags";
            case STRINGS -> "custom model data strings";
            case COLORS -> "custom model data colors";
            case SINGLE_INT -> "custom model data";
        } + " of " + getExpr().toString(event, debug);
    }

    private Object[] empty() {
        return (Object[]) Array.newInstance(returnType, 0);
    }

    private void clearType(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
        switch (dataType) {
            case FLOATS -> floats.clear();
            case FLAGS -> flags.clear();
            case STRINGS -> strings.clear();
            case COLORS -> colors.clear();
            case ALL, SINGLE_INT -> {
            }
        }
    }

    private <T> void apply(List<T> values, T value, ChangeMode mode) {
        switch (mode) {
            case ADD, SET -> values.add(value);
            case REMOVE -> values.remove(value);
            default -> {
            }
        }
    }
}
