package ch.njol.skript.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Holding")
@Description("Checks whether a living entity is holding a specific item.")
@Example("player is holding a stick")
@Example("victim isn't holding a diamond sword in off-hand")
@Since("1.0")
public final class CondItemInHand extends Condition {

    private Expression<LivingEntity> entities;
    private Expression<FabricItemType> items;
    private boolean offHand;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        items = (Expression<FabricItemType>) exprs[1];
        offHand = matchedPattern == 2 || matchedPattern == 3 || matchedPattern == 6 || matchedPattern == 7;
        setNegated(matchedPattern >= 4);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return entities.check(event, entity -> items.check(event, item -> matches(entity, item)));
    }

    private boolean matches(LivingEntity entity, FabricItemType itemType) {
        ItemStack held = offHand ? entity.getOffhandItem() : entity.getMainHandItem();
        return itemType.isOfType(held);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return entities.toString(event, debug) + " "
                + (entities.isSingle() ? "is" : "are")
                + (isNegated() ? " not holding " : " holding ")
                + items.toString(event, debug)
                + (offHand ? " in off-hand" : "");
    }
}
