package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item with Custom Model Data")
@Description("Get an item with custom model data.")
@Example("give player a diamond sword with custom model data 2")
@Example("set slot 1 of inventory of player to wooden hoe with custom model data 357")
@Example("give player a diamond hoe with custom model data 2, true, true, \"scythe\", and rgb(0,0,100)")
@RequiredPlugins("Minecraft 1.21.4+ (boolean/string/color support)")
@Since({"2.5", "2.12 (boolean/string/color support)"})
public class ExprItemWithCustomModelData extends PropertyExpression<FabricItemType, FabricItemType> {

    static {
        Skript.registerExpression(ExprItemWithCustomModelData.class, FabricItemType.class,
                "%itemtype% with [custom] model data %numbers/booleans/strings/colors%");
    }

    private Expression<?> data;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        setExpr((Expression<FabricItemType>) exprs[0]);
        data = exprs[1];
        return true;
    }

    @Override
    protected FabricItemType[] get(SkriptEvent event, FabricItemType[] source) {
        Object[] values = data.getArray(event);
        if (values.length == 0) {
            return source;
        }

        List<Float> floats = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Number number) {
                floats.add(number.floatValue());
            } else if (value instanceof Boolean flag) {
                flags.add(flag);
            } else if (value instanceof String string) {
                strings.add(string);
            } else if (value instanceof Color color) {
                colors.add(color.rgb());
            }
        }

        return get(source, item -> {
            ItemStack stack = item.toStack();
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
            return new FabricItemType(stack);
        });
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr().toString(event, debug) + " with custom model data " + data.toString(event, debug);
    }
}
