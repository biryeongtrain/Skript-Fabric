package org.skriptlang.skript.fabric.compat;

import ch.njol.skript.util.Color;
import net.minecraft.world.item.component.FireworkExplosion;

public record FabricFireworkEffect(
        FireworkExplosion.Shape shape,
        Color[] colors,
        Color[] fadeColors,
        boolean flicker,
        boolean trail
) {
}
