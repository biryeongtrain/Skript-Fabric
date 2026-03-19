package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Name("Tool")
@Description("The item a living entity is holding in its main or off hand.")
@Example("player's tool is a diamond sword")
@Example("set off hand tool of victim to shield")
@Since("1.0")
public final class ExprTool extends PropertyExpression<Entity, Slot> {

    static {
        Skript.registerExpression(
                ExprTool.class,
                Slot.class,
                "[the] (tool|held item|weapon) [of %entities%]",
                "%entities%'[s] (tool|held item|weapon)",
                "[the] off[ ]hand (tool|item) [of %entities%]",
                "%entities%'[s] off[ ]hand (tool|item)"
        );
    }

    private boolean offHand;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends Entity>) expressions[0]);
        offHand = matchedPattern >= 2;
        return true;
    }

    @Override
    protected Slot[] get(SkriptEvent event, Entity[] source) {
        InteractionHand hand = offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        return get(source, entity -> {
            if (!(entity instanceof LivingEntity living)) {
                return null;
            }
            return new HandBackedSlot(living, hand);
        });
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (offHand ? "off hand " : "") + "tool of " + getExpr().toString(event, debug);
    }

    private static final class HandBackedSlot extends Slot {

        private final LivingEntity entity;
        private final InteractionHand hand;
        private final EquipmentSlot equipmentSlot;

        private HandBackedSlot(LivingEntity entity, InteractionHand hand) {
            super(new SimpleContainer(1), hand == InteractionHand.MAIN_HAND ? 0 : 1, 0, 0);
            this.entity = entity;
            this.hand = hand;
            this.equipmentSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        }

        @Override
        public ItemStack getItem() {
            ItemStack stack = entity.getItemBySlot(equipmentSlot);
            return stack == null ? ItemStack.EMPTY : stack;
        }

        @Override
        public void set(ItemStack stack) {
            ItemStack next = stack == null ? ItemStack.EMPTY : stack;
            if (!setEquipmentDirectly(entity, equipmentSlot, next)) {
                entity.setItemInHand(hand, next);
            }
            setChanged();
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        private static boolean setEquipmentDirectly(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
            Object equipment = equipmentHolder(entity);
            if (equipment == null) {
                return false;
            }
            try {
                Method set = equipment.getClass().getDeclaredMethod("set", EquipmentSlot.class, ItemStack.class);
                set.setAccessible(true);
                set.invoke(equipment, slot, stack);
                return true;
            } catch (ReflectiveOperationException exception) {
                return false;
            }
        }

        private static @Nullable Object equipmentHolder(LivingEntity entity) {
            Class<?> type = entity.getClass();
            while (type != null) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.getType().getName().equals("net.minecraft.world.entity.EntityEquipment")) {
                        try {
                            field.setAccessible(true);
                            return field.get(entity);
                        } catch (ReflectiveOperationException exception) {
                            return null;
                        }
                    }
                }
                type = type.getSuperclass();
            }
            return null;
        }
    }
}
