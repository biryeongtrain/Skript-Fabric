package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

public final class VectorClassInfo {

    private VectorClassInfo() {
    }

    public static void register() {
        ClassInfo<Vec3> info = new ClassInfo<>(Vec3.class);
        info.setPropertyInfo(Property.WXYZ, new VectorWXYZHandler());
        Classes.registerClassInfo(info);
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
