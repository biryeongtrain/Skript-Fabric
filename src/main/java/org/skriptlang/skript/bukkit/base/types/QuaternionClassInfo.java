package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

public final class QuaternionClassInfo {

    private static final Pattern QUATERNION_PATTERN = Pattern.compile(
            "^\\s*quaternion\\s*(?:from\\s+)?\\(?\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\)?\\s*$"
    );

    private QuaternionClassInfo() {
    }

    public static void register() {
        ClassInfo<Quaternionf> info = new ClassInfo<>(Quaternionf.class, "quaternion");
        info.setParser(new Parser());
        info.setPropertyInfo(Property.WXYZ, new QuaternionWXYZHandler());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<Quaternionf> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable Quaternionf parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            Matcher matcher = QUATERNION_PATTERN.matcher(input.trim().toLowerCase(Locale.ENGLISH));
            if (!matcher.matches()) {
                return null;
            }
            try {
                return new Quaternionf(
                        Float.parseFloat(matcher.group(1)),
                        Float.parseFloat(matcher.group(2)),
                        Float.parseFloat(matcher.group(3)),
                        Float.parseFloat(matcher.group(4))
                );
            } catch (NumberFormatException exception) {
                return null;
            }
        }
    }

    public static final class QuaternionWXYZHandler extends WXYZHandler<Quaternionf, Double> {

        @Override
        public PropertyHandler<Quaternionf> newInstance() {
            QuaternionWXYZHandler handler = new QuaternionWXYZHandler();
            handler.axis = axis;
            return handler;
        }

        @Override
        public boolean supportsAxis(Axis axis) {
            return true;
        }

        @Override
        public @Nullable Double convert(Quaternionf propertyHolder) {
            if (axis == null) {
                return null;
            }
            return switch (axis) {
                case W -> (double) propertyHolder.w;
                case X -> (double) propertyHolder.x;
                case Y -> (double) propertyHolder.y;
                case Z -> (double) propertyHolder.z;
            };
        }

        @Override
        public Class<Double> returnType() {
            return Double.class;
        }
    }
}
