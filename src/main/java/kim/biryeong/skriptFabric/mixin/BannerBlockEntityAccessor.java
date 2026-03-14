package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BannerBlockEntity.class)
public interface BannerBlockEntityAccessor {

	@Accessor("patterns")
	void skript$setPatterns(BannerPatternLayers patterns);
}
