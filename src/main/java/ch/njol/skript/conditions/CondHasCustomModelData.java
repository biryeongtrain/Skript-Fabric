package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Has Custom Model Data")
@Description("Check if an item has a custom model data tag")
@Example("player's tool has custom model data")
@Example("""
        if player's tool has custom model data flags:
            loop custom model data flags of player's tool:
                send "Flag %loop-index%: %loop-value%"
        """)
@Example("set {_coloured} to whether player's tool has model data colours")
@Since("2.5, 2.12 (expanded data types)")
@RequiredPlugins("Minecraft 1.21.4+ (floats/flags/strings/colours)")
public class CondHasCustomModelData extends PropertyCondition<FabricItemType> {

    static {
        register(CondHasCustomModelData.class, PropertyType.HAVE, "[custom] model data [1:floats|2:flags|3:strings|4:colo[u]rs]", "itemtypes");
    }

    private enum CMDType {
        ANY,
        FLOATS,
        FLAGS,
        STRINGS,
        COLORS
    }

    private CMDType dataType;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        dataType = CMDType.values()[parseResult.mark];
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public boolean check(FabricItemType item) {
        CustomModelData component = item.toStack().get(DataComponents.CUSTOM_MODEL_DATA);
        if (component == null) {
            return false;
        }
        return switch (dataType) {
            case ANY -> true;
            case FLOATS -> !component.floats().isEmpty();
            case FLAGS -> !component.flags().isEmpty();
            case STRINGS -> !component.strings().isEmpty();
            case COLORS -> !component.colors().isEmpty();
        };
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.HAVE;
    }

    @Override
    protected String getPropertyName() {
        return "custom model data" + (dataType != CMDType.ANY ? " " + dataType.name().toLowerCase(Locale.ENGLISH) : "");
    }
}
