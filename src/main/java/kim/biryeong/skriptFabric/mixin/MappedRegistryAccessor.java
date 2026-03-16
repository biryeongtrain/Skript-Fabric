package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor for MappedRegistry's frozen field.
 * Used to temporarily unfreeze dynamic registries for custom enchantment registration.
 */
@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor {

	@Accessor("frozen")
	void skript$setFrozen(boolean frozen);

	@Accessor("frozen")
	boolean skript$isFrozen();

}
