package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Armor Slot")
@Description("Armor equipment slots of living entities.")
@Example("set chestplate of the player to diamond chestplate")
@Keywords("armor")
@Since("1.0")
public final class ExprArmorSlot extends PropertyExpression<LivingEntity, Slot> {

    private static final Set<EquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    );

    static {
        Skript.registerExpression(
                ExprArmorSlot.class,
                Slot.class,
                PropertyExpression.getPatterns("armo[u]r[s] [item:item[s]]", "livingentities")
        );
        Skript.registerExpression(
                ExprArmorSlot.class,
                Slot.class,
                PropertyExpression.getPatterns("%-*equipmentslots% [item:item[s]]", "livingentities")
        );
    }

    private @Nullable Literal<EquipmentSlot> requestedSlots;
    private boolean explicitItem;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length > 1 && expressions[0] != null) {
            requestedSlots = (Literal<EquipmentSlot>) expressions[0];
        }
        explicitItem = parseResult.hasTag("item");
        setExpr((Expression<? extends LivingEntity>) expressions[expressions.length - 1]);
        return true;
    }

    @Override
    protected Slot[] get(SkriptEvent event, LivingEntity[] source) {
        List<Slot> slots = new ArrayList<>();
        EquipmentSlot[] selected = requestedSlots == null ? ARMOR_SLOTS.toArray(EquipmentSlot[]::new) : requestedSlots.getArray(event);
        for (LivingEntity entity : source) {
            for (EquipmentSlot slot : selected) {
                if (ARMOR_SLOTS.contains(slot)) {
                    slots.add(new ArmorBackedSlot(entity, slot));
                }
            }
        }
        return slots.toArray(Slot[]::new);
    }

    @Override
    public boolean isSingle() {
        return requestedSlots != null && requestedSlots.isSingle() && getExpr().isSingle();
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        if (requestedSlots == null) {
            builder.append("armor");
        } else {
            builder.append(requestedSlots);
        }
        if (explicitItem) {
            builder.append("items");
        }
        builder.append("of", getExpr());
        return builder.toString();
    }

    private static final class ArmorBackedSlot extends Slot {

        private final LivingEntity entity;
        private final EquipmentSlot slot;

        private ArmorBackedSlot(LivingEntity entity, EquipmentSlot slot) {
            super(new SimpleContainer(1), slot.getIndex(), 0, 0);
            this.entity = entity;
            this.slot = slot;
        }

        @Override
        public ItemStack getItem() {
            return entity.getItemBySlot(slot);
        }

        @Override
        public void set(ItemStack stack) {
            entity.setItemSlot(slot, stack == null ? ItemStack.EMPTY : stack);
            setChanged();
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }
    }
}
