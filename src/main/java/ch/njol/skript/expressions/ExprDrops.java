package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDrops extends SimpleExpression<FabricItemType> {

    private static final @Nullable Class<?> ENTITY_DEATH_EVENT =
            ExpressionHandleSupport.resolveClass("ch.njol.skript.effects.FabricEffectEventHandles$EntityDeath");

    static {
        Skript.registerExpression(ExprDrops.class, FabricItemType.class, "[the] drops");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (ENTITY_DEATH_EVENT == null || !getParser().isCurrentEvent(ENTITY_DEATH_EVENT)) {
            Skript.error("The expression 'drops' may only be used in a death event");
            return false;
        }
        return true;
    }

    @Override
    protected FabricItemType @Nullable [] get(SkriptEvent event) {
        Object drops = ExpressionHandleSupport.invoke(event.handle(), "drops");
        if (!(drops instanceof List<?> list)) {
            return null;
        }
        return list.stream()
                .filter(ItemStack.class::isInstance)
                .map(ItemStack.class::cast)
                .map(FabricItemType::new)
                .toArray(FabricItemType[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{FabricItemType[].class, Experience[].class};
            default -> null;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Object rawDrops = ExpressionHandleSupport.invoke(event.handle(), "drops");
        if (!(rawDrops instanceof List<?> rawList) || delta == null) {
            return;
        }
        List<ItemStack> drops = (List<ItemStack>) rawList;
        List<FabricItemType> itemTypes = new ArrayList<>();
        int experience = 0;
        boolean touchesExperience = false;
        for (Object value : delta) {
            if (value instanceof FabricItemType itemType) {
                itemTypes.add(itemType);
            } else if (value instanceof Experience xp) {
                experience += xp.getXP();
                touchesExperience = true;
            }
        }
        if (mode == ChangeMode.SET) {
            drops.clear();
        }
        if (touchesExperience) {
            int current = ((Number) ExpressionHandleSupport.invoke(event.handle(), "droppedExp")).intValue();
            int updated = mode == ChangeMode.REMOVE ? Math.max(0, current - experience)
                    : mode == ChangeMode.ADD ? current + experience
                    : experience;
            ExpressionHandleSupport.set(event.handle(), "setDroppedExp", updated);
        }
        for (FabricItemType itemType : itemTypes) {
            if (mode == ChangeMode.ADD || mode == ChangeMode.SET) {
                drops.add(itemType.toStack());
            } else {
                removeOne(drops, itemType);
            }
        }
    }

    private void removeOne(List<ItemStack> stacks, FabricItemType type) {
        for (int i = 0; i < stacks.size(); i++) {
            if (type.matches(stacks.get(i))) {
                stacks.remove(i);
                return;
            }
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the drops";
    }
}
