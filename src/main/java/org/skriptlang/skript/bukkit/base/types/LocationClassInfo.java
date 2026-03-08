package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;
import net.minecraft.world.phys.Vec3;

public final class LocationClassInfo {

    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "^\\(?\\s*(-?\\d+(?:\\.\\d+)?)\\s*(?:,\\s*|\\s+)(-?\\d+(?:\\.\\d+)?)\\s*(?:,\\s*|\\s+)(-?\\d+(?:\\.\\d+)?)\\s*\\)?$"
    );

    private LocationClassInfo() {
    }

    public static void register() {
        ClassInfo<FabricLocation> info = new ClassInfo<>(FabricLocation.class, "location");
        info.setParser(new LocationParser());
        info.setPropertyInfo(Property.WXYZ, new LocationWXYZHandler());
        Classes.registerClassInfo(info);
    }

    private static class LocationParser implements ClassInfo.Parser<FabricLocation> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable FabricLocation parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }

            Matcher matcher = LOCATION_PATTERN.matcher(input.trim());
            if (!matcher.matches()) {
                return null;
            }

            try {
                double x = Double.parseDouble(matcher.group(1));
                double y = Double.parseDouble(matcher.group(2));
                double z = Double.parseDouble(matcher.group(3));
                return new FabricLocation(null, new Vec3(x, y, z));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    public static class LocationWXYZHandler extends WXYZHandler<FabricLocation, Double> {

        @Override
        public PropertyHandler<FabricLocation> newInstance() {
            LocationWXYZHandler handler = new LocationWXYZHandler();
            handler.axis = axis;
            return handler;
        }

        @Override
        public boolean supportsAxis(Axis axis) {
            return axis != Axis.W;
        }

        @Override
        public @Nullable Double convert(FabricLocation propertyHolder) {
            if (axis == null) {
                return null;
            }
            return switch (axis) {
                case W -> null;
                case X -> propertyHolder.position().x;
                case Y -> propertyHolder.position().y;
                case Z -> propertyHolder.position().z;
            };
        }

        @Override
        public Class<Double> returnType() {
            return Double.class;
        }
    }
}
