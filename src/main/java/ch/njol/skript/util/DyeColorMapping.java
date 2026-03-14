package ch.njol.skript.util;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Bidirectional mapping between Minecraft {@link DyeColor} and Skript {@link Color}.
 */
public final class DyeColorMapping {

	private static final Map<DyeColor, ColorRGB> DYE_TO_COLOR = new EnumMap<>(DyeColor.class);
	private static final Map<Integer, DyeColor> RGB_TO_DYE = new HashMap<>();

	static {
		for (DyeColor dye : DyeColor.values()) {
			int argb = dye.getTextureDiffuseColor();
			ColorRGB color = new ColorRGB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
			DYE_TO_COLOR.put(dye, color);
			RGB_TO_DYE.put(color.rgb(), dye);
		}
	}

	private DyeColorMapping() {
	}

	/**
	 * Converts a {@link DyeColor} to its corresponding {@link ColorRGB}.
	 */
	public static ColorRGB toColor(DyeColor dye) {
		return DYE_TO_COLOR.get(dye);
	}

	/**
	 * Converts a {@link Color} to the nearest {@link DyeColor}.
	 * Returns an exact match if available, otherwise finds the closest by Euclidean distance.
	 */
	public static @Nullable DyeColor toDyeColor(Color color) {
		DyeColor exact = RGB_TO_DYE.get(color.rgb());
		if (exact != null)
			return exact;

		int r = color.red(), g = color.green(), b = color.blue();
		DyeColor nearest = null;
		int minDist = Integer.MAX_VALUE;
		for (Map.Entry<DyeColor, ColorRGB> entry : DYE_TO_COLOR.entrySet()) {
			ColorRGB c = entry.getValue();
			int dr = r - c.red(), dg = g - c.green(), db = b - c.blue();
			int dist = dr * dr + dg * dg + db * db;
			if (dist < minDist) {
				minDist = dist;
				nearest = entry.getKey();
			}
		}
		return nearest;
	}

	/**
	 * Returns the {@link DyeColor} whose RGB exactly matches the given color, or null.
	 */
	public static @Nullable DyeColor toDyeColorExact(Color color) {
		return RGB_TO_DYE.get(color.rgb());
	}
}
