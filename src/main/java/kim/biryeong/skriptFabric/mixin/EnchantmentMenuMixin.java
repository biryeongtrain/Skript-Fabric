package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

	@Shadow @Final public int[] costs;
	@Shadow @Final public int[] enchantClue;
	@Shadow @Final public int[] levelClue;
	@Shadow @Final private ContainerLevelAccess access;
	@Shadow @Final private net.minecraft.world.Container enchantSlots;

	@Unique
	private Player skript$player;

	/**
	 * Capture the player from the Inventory parameter in the constructor.
	 */
	@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
			at = @At("RETURN"))
	private void skriptFabric$capturePlayer(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
		this.skript$player = inventory.player;
	}

	/**
	 * After the enchantment menu recalculates its offerings (slotsChanged),
	 * dispatch an enchant prepare event with the enchantment offers.
	 */
	@Inject(method = "slotsChanged", at = @At("RETURN"))
	private void skriptFabric$onSlotsChanged(net.minecraft.world.Container container, CallbackInfo ci) {
		access.execute((level, pos) -> {
			if (!(level instanceof ServerLevel serverLevel)) return;

			ItemStack item = enchantSlots.getItem(0);
			if (item.isEmpty()) return;

			// Calculate approximate bookshelf bonus from costs
			int bonus = 0;
			for (int cost : costs) {
				if (cost > 0) bonus = Math.max(bonus, cost);
			}

			// Build offer list from enchantClue/levelClue
			Registry<Enchantment> reg = serverLevel.registryAccess()
					.lookupOrThrow(Registries.ENCHANTMENT);
			List<EnchantmentInstance> offers = new ArrayList<>();
			for (int i = 0; i < enchantClue.length; i++) {
				int clueId = enchantClue[i];
				if (clueId >= 0) {
					int clueLevel = (i < levelClue.length) ? levelClue[i] : 1;
					reg.get(clueId).ifPresent(holder ->
							offers.add(new EnchantmentInstance(holder, clueLevel)));
				}
			}

			ServerPlayer serverPlayer = (skript$player instanceof ServerPlayer sp) ? sp : null;
			SkriptFabricEventBridge.dispatchEnchantPrepare(serverLevel, serverPlayer, item, bonus, offers);
		});
	}

	/**
	 * After a player clicks a button to enchant, dispatch an enchant apply event
	 * with the selected enchantment data.
	 */
	@Inject(method = "clickMenuButton", at = @At("RETURN"))
	private void skriptFabric$onClickMenuButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;

		ServerLevel level = serverPlayer.level();
		ItemStack item = enchantSlots.getItem(0);

		int expCost = (id >= 0 && id < costs.length) ? costs[id] : 0;

		// Build the enchantment list from enchantClue/levelClue for the selected slot
		List<EnchantmentInstance> enchantments = new ArrayList<>();
		if (id >= 0 && id < enchantClue.length) {
			int clueId = enchantClue[id];
			int clueLevel = (id < levelClue.length) ? levelClue[id] : 1;
			if (clueId >= 0) {
				Registry<Enchantment> reg = level.registryAccess()
						.lookupOrThrow(Registries.ENCHANTMENT);
				reg.get(clueId).ifPresent(holder ->
						enchantments.add(new EnchantmentInstance(holder, clueLevel)));
			}
		}

		SkriptFabricEventBridge.dispatchEnchantApply(level, serverPlayer, item, enchantments, expCost);
	}

}
