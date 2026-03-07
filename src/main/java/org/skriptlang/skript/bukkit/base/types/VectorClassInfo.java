package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

public final class VectorClassInfo {

    private static final Pattern VECTOR_PATTERN = Pattern.compile(
            "^\\s*vector\\s*(?:from\\s+)?\\(?\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\)?\\s*$"
    );

    private VectorClassInfo() {
    }

    public static void register() {
        ClassInfo<Vec3> info = new ClassInfo<>(Vec3.class);
        info.setParser(new Parser());
        info.setPropertyInfo(Property.WXYZ, new VectorWXYZHandler());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<Vec3> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable Vec3 parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            Matcher matcher = VECTOR_PATTERN.matcher(input.trim().toLowerCase(Locale.ENGLISH));
            if (!matcher.matches()) {
                return null;
            }
            try {
                return new Vec3(
                        Double.parseDouble(matcher.group(1)),
                        Double.parseDouble(matcher.group(2)),
                        Double.parseDouble(matcher.group(3))
                );
            } catch (NumberFormatException exception) {
                return null;
            }
        }
    }

    public static class VectorWXYZHandler extends WXYZHandler<Vec3, Double> {

        @Override
        public PropertyHandler<Vec3> newInstance() {
            VectorWXYZHandler handler = new VectorWXYZHandler();
            handler.axis = axis;
            return handler;
        }

        @Override
        public boolean supportsAxis(Axis axis) {
            return axis != Axis.W;
        }

        @Override
        public @Nullable Double convert(Vec3 propertyHolder) {
            if (axis == null) {
                return null;
            }
            return switch (axis) {
                case W -> null;
                case X -> propertyHolder.x;
                case Y -> propertyHolder.y;
                case Z -> propertyHolder.z;
            };
        }

        @Override
        public Class<Double> returnType() {
            return Double.class;
        }
    }
}
