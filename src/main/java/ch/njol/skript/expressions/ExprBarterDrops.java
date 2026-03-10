package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBarterDrops extends SimpleExpression<FabricItemType> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprBarterDrops.class, FabricItemType.class, "[the] [piglin] barter[ing] drops");
    }

    private Kleenean delay = Kleenean.FALSE;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        delay = isDelayed;
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.PiglinBarter.class};
    }

    @Override
    protected FabricItemType @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.PiglinBarter handle)) {
            return null;
        }
        return handle.outcome().stream().map(FabricItemType::new).toArray(FabricItemType[]::new);
    }

    @Override
    public @Nullable Iterator<? extends FabricItemType> iterator(SkriptEvent event) {
        FabricItemType[] values = get(event);
        return values == null ? null : java.util.Arrays.asList(values).iterator();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (!delay.isFalse()) {
            Skript.error("Can't change the piglin bartering drops after the event has already passed");
            return null;
        }
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE -> new Class[]{FabricItemType[].class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricEventCompatHandles.PiglinBarter handle)) {
            return;
        }
        List<ItemStack> outcome = handle.outcome();
        if (mode == ChangeMode.DELETE) {
            outcome.clear();
            return;
        }
        if (delta == null) {
            return;
        }
        if (mode == ChangeMode.SET) {
            outcome.clear();
        }
        for (Object value : delta) {
            FabricItemType type = (FabricItemType) value;
            if (mode == ChangeMode.ADD || mode == ChangeMode.SET) {
                outcome.add(type.toStack());
                continue;
            }
            removeOne(outcome, type);
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
        return "the barter drops";
    }
}
