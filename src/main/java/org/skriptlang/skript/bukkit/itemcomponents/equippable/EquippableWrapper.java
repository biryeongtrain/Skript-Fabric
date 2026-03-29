package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class EquippableWrapper extends ComponentWrapper<Equippable> {

    private final @Nullable ItemStack itemStack;
    private final @Nullable Slot slot;
    private final @Nullable FabricItemType itemType;
    private @Nullable Equippable component;

    public EquippableWrapper(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.slot = null;
        this.itemType = null;
        this.component = null;
    }

    public EquippableWrapper(Slot slot) {
        this.itemStack = null;
        this.slot = slot;
        this.itemType = null;
        this.component = null;
    }

    public EquippableWrapper(FabricItemType itemType) {
        this.itemStack = null;
        this.slot = null;
        this.itemType = itemType;
        this.component = null;
    }

    public EquippableWrapper(Equippable component) {
        this.itemStack = null;
        this.slot = null;
        this.itemType = null;
        this.component = component;
    }

    @Override
    public Equippable getComponent() {
        if (slot != null) {
            ItemStack stack = slot.getItem();
            return stack.isEmpty() ? defaultComponent() : fromStack(stack);
        }
        if (itemStack != null) {
            return fromStack(itemStack);
        }
        if (itemType != null) {
            Equippable equippable = itemType.equippable();
            return equippable != null ? equippable : fromStack(itemType.toStack());
        }
        return component != null ? component : defaultComponent();
    }

    @Override
    public void applyComponent(Equippable component) {
        if (slot != null) {
            ItemStack stack = slot.getItem().copy();
            if (!stack.isEmpty()) {
                stack.set(DataComponents.EQUIPPABLE, component);
                slot.set(stack);
            }
        } else if (itemStack != null) {
            itemStack.set(DataComponents.EQUIPPABLE, component);
        } else if (itemType != null) {
            itemType.equippable(component);
        }
        this.component = component;
    }

    public void clearComponent() {
        if (slot != null) {
            ItemStack stack = slot.getItem().copy();
            if (!stack.isEmpty()) {
                stack.remove(DataComponents.EQUIPPABLE);
                slot.set(stack);
            }
        } else if (itemStack != null) {
            itemStack.remove(DataComponents.EQUIPPABLE);
        } else if (itemType != null) {
            itemType.equippable(null);
        }
        component = defaultComponent();
    }

    @Override
    public EquippableWrapper copy() {
        return new EquippableWrapper(copyComponent(getComponent(), getComponent().slot()));
    }

    public EquipmentSlot slot() {
        return getComponent().slot();
    }

    public void slot(EquipmentSlot slot) {
        applyComponent(copyComponent(getComponent(), slot));
    }

    public Holder<SoundEvent> equipSound() {
        return getComponent().equipSound();
    }

    public void equipSound(Holder<SoundEvent> sound) {
        Equippable source = getComponent();
        applyComponent(build(
                source.slot(),
                sound,
                source.assetId().orElse(null),
                source.cameraOverlay().orElse(null),
                source.allowedEntities().orElse(null),
                source.dispensable(),
                source.swappable(),
                source.damageOnHurt(),
                source.equipOnInteract(),
                source.canBeSheared(),
                source.shearingSound()
        ));
    }

    public Holder<SoundEvent> shearSound() {
        return getComponent().shearingSound();
    }

    public void shearSound(Holder<SoundEvent> sound) {
        Equippable source = getComponent();
        applyComponent(build(
                source.slot(),
                source.equipSound(),
                source.assetId().orElse(null),
                source.cameraOverlay().orElse(null),
                source.allowedEntities().orElse(null),
                source.dispensable(),
                source.swappable(),
                source.damageOnHurt(),
                source.equipOnInteract(),
                source.canBeSheared(),
                sound
        ));
    }

    public @Nullable Identifier cameraOverlay() {
        return getComponent().cameraOverlay().orElse(null);
    }

    public void cameraOverlay(@Nullable Identifier overlay) {
        Equippable source = getComponent();
        applyComponent(build(
                source.slot(),
                source.equipSound(),
                source.assetId().orElse(null),
                overlay,
                source.allowedEntities().orElse(null),
                source.dispensable(),
                source.swappable(),
                source.damageOnHurt(),
                source.equipOnInteract(),
                source.canBeSheared(),
                source.shearingSound()
        ));
    }

    public @Nullable Identifier model() {
        return getComponent().assetId().map(ResourceKey::identifier).orElse(null);
    }

    public void model(@Nullable Identifier assetId) {
        Equippable source = getComponent();
        ResourceKey<EquipmentAsset> key = assetId != null ? EquipmentAssets.createId(assetId.toString()) : null;
        applyComponent(build(
                source.slot(),
                source.equipSound(),
                key,
                source.cameraOverlay().orElse(null),
                source.allowedEntities().orElse(null),
                source.dispensable(),
                source.swappable(),
                source.damageOnHurt(),
                source.equipOnInteract(),
                source.canBeSheared(),
                source.shearingSound()
        ));
    }

    public Collection<EntityType<?>> allowedEntities() {
        HolderSet<EntityType<?>> allowed = getComponent().allowedEntities().orElse(null);
        if (allowed == null) {
            return List.of();
        }
        List<EntityType<?>> results = new ArrayList<>();
        for (Holder<EntityType<?>> holder : allowed) {
            results.add(holder.value());
        }
        return results;
    }

    public void allowedEntities(Collection<EntityType<?>> types) {
        Equippable source = getComponent();
        HolderSet<EntityType<?>> holders = types.isEmpty() ? null : HolderSet.direct(types.stream().map(BuiltInRegistries.ENTITY_TYPE::wrapAsHolder).toList());
        applyComponent(build(
                source.slot(),
                source.equipSound(),
                source.assetId().orElse(null),
                source.cameraOverlay().orElse(null),
                holders,
                source.dispensable(),
                source.swappable(),
                source.damageOnHurt(),
                source.equipOnInteract(),
                source.canBeSheared(),
                source.shearingSound()
        ));
    }

    public void damageOnHurt(boolean value) {
        applyComponent(buildFromBooleans(value, getComponent().dispensable(), getComponent().swappable(), getComponent().equipOnInteract(), getComponent().canBeSheared()));
    }

    public void dispensable(boolean value) {
        applyComponent(buildFromBooleans(getComponent().damageOnHurt(), value, getComponent().swappable(), getComponent().equipOnInteract(), getComponent().canBeSheared()));
    }

    public void swappable(boolean value) {
        applyComponent(buildFromBooleans(getComponent().damageOnHurt(), getComponent().dispensable(), value, getComponent().equipOnInteract(), getComponent().canBeSheared()));
    }

    public void equipOnInteract(boolean value) {
        applyComponent(buildFromBooleans(getComponent().damageOnHurt(), getComponent().dispensable(), getComponent().swappable(), value, getComponent().canBeSheared()));
    }

    public void canBeSheared(boolean value) {
        applyComponent(buildFromBooleans(getComponent().damageOnHurt(), getComponent().dispensable(), getComponent().swappable(), getComponent().equipOnInteract(), value));
    }

    private Equippable buildFromBooleans(boolean damageOnHurt, boolean dispensable, boolean swappable, boolean equipOnInteract, boolean canBeSheared) {
        Equippable source = getComponent();
        return build(
                source.slot(),
                source.equipSound(),
                source.assetId().orElse(null),
                source.cameraOverlay().orElse(null),
                source.allowedEntities().orElse(null),
                dispensable,
                swappable,
                damageOnHurt,
                equipOnInteract,
                canBeSheared,
                source.shearingSound()
        );
    }

    private static Equippable fromStack(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null ? equippable : defaultComponent();
    }

    private static Equippable copyComponent(Equippable source, EquipmentSlot slot) {
        return build(
                slot,
                source.equipSound(),
                source.assetId().orElse(null),
                source.cameraOverlay().orElse(null),
                source.allowedEntities().orElse(null),
                source.dispensable(),
                source.swappable(),
                source.damageOnHurt(),
                source.equipOnInteract(),
                source.canBeSheared(),
                source.shearingSound()
        );
    }

    private static Equippable build(
            EquipmentSlot slot,
            Holder<SoundEvent> equipSound,
            @Nullable ResourceKey<EquipmentAsset> assetId,
            @Nullable Identifier cameraOverlay,
            @Nullable HolderSet<EntityType<?>> allowedEntities,
            boolean dispensable,
            boolean swappable,
            boolean damageOnHurt,
            boolean equipOnInteract,
            boolean canBeSheared,
            Holder<SoundEvent> shearingSound
    ) {
        Equippable.Builder builder = Equippable.builder(slot)
                .setEquipSound(equipSound)
                .setDispensable(dispensable)
                .setSwappable(swappable)
                .setDamageOnHurt(damageOnHurt)
                .setEquipOnInteract(equipOnInteract)
                .setCanBeSheared(canBeSheared)
                .setShearingSound(shearingSound);
        if (assetId != null) {
            builder.setAsset(assetId);
        }
        if (cameraOverlay != null) {
            builder.setCameraOverlay(cameraOverlay);
        }
        if (allowedEntities != null) {
            builder.setAllowedEntities(allowedEntities);
        }
        return builder.build();
    }

    private static Equippable defaultComponent() {
        return Equippable.builder(EquipmentSlot.HEAD)
                .setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC)
                .setDispensable(true)
                .setSwappable(true)
                .setDamageOnHurt(false)
                .setEquipOnInteract(false)
                .setCanBeSheared(false)
                .setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEEP_SHEAR))
                .build();
    }
}
