package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprItemOfEntity extends SimpleExpression<Slot> {

    private Expression<Entity> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        return true;
    }

    @Override
    protected Slot @Nullable [] get(SkriptEvent event) {
        List<Slot> results = new ArrayList<>();
        for (Entity entity : entities.getAll(event)) {
            Slot slot = createSlot(entity);
            if (slot != null) {
                results.add(slot);
            }
        }
        return results.toArray(Slot[]::new);
    }

    private @Nullable Slot createSlot(Entity entity) {
        if (entity instanceof ItemFrame itemFrame) {
            return new EntitySlot(() -> itemFrame.getItem(), itemFrame::setItem);
        }
        if (entity instanceof ItemEntity itemEntity) {
            return new EntitySlot(itemEntity::getItem, itemEntity::setItem);
        }
        if (entity instanceof ThrowableItemProjectile throwable) {
            return new EntitySlot(throwable::getItem, throwable::setItem);
        }
        if (entity instanceof AbstractArrow arrow) {
            return new SlotAccessBackedSlot(arrow.getSlot(0));
        }
        if (entity instanceof Display.ItemDisplay display) {
            return new SlotAccessBackedSlot(display.getSlot(0));
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return entities.isSingle();
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "item of " + entities.toString(event, debug);
    }

    private static final class EntitySlot extends Slot {

        private final java.util.function.Supplier<ItemStack> getter;
        private final java.util.function.Consumer<ItemStack> setter;

        private EntitySlot(java.util.function.Supplier<ItemStack> getter, java.util.function.Consumer<ItemStack> setter) {
            super(new SimpleContainer(1), 0, 0, 0);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public ItemStack getItem() {
            return getter.get();
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            setter.accept(stack);
        }
    }

    private static final class SlotAccessBackedSlot extends Slot {

        private final SlotAccess access;

        private SlotAccessBackedSlot(SlotAccess access) {
            super(new SimpleContainer(1), 0, 0, 0);
            this.access = access;
        }

        @Override
        public ItemStack getItem() {
            return access.get();
        }

        @Override
        public boolean hasItem() {
            return !access.get().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            access.set(stack);
        }
    }
}
