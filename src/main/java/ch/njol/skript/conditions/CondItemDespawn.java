package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.entity.item.ItemEntity;

@Name("Will Despawn")
@Description("Checks if the dropped item will be despawned naturally through Minecraft's timer.")
@Example("""
    if all dropped items can despawn naturally:
        prevent all dropped items from naturally despawning
    """)
@Since("2.11")
public class CondItemDespawn extends PropertyCondition<ItemEntity> {

    private PropertyType propertyType = PropertyType.WILL;

    static {
        register(CondItemDespawn.class, PropertyType.WILL, "(despawn naturally|naturally despawn)", "itementities");
        register(CondItemDespawn.class, PropertyType.CAN, "(despawn naturally|naturally despawn)", "itementities");
    }

    @Override
    public boolean init(ch.njol.skript.lang.Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, ch.njol.skript.lang.SkriptParser.ParseResult parseResult) {
        propertyType = matchedPattern < 2 ? PropertyType.WILL : PropertyType.CAN;
        return super.init(expressions, matchedPattern % 2, isDelayed, parseResult);
    }

    @Override
    public boolean check(ItemEntity item) {
        return !ConditionRuntimeSupport.booleanMethod(item, false, "isUnlimitedLifetime", "hasUnlimitedLifetime")
                && !ConditionRuntimeSupport.booleanField(item, false, "unlimitedLifetime");
    }

    @Override
    protected PropertyType getPropertyType() {
        return propertyType;
    }

    @Override
    protected String getPropertyName() {
        return "naturally despawn";
    }
}
