package ch.njol.skript.registrations;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.fabric.compat.FabricLocation;

// When using these fields, be aware all ClassInfo's must be registered!
public class DefaultClasses {

    public static ClassInfo<Object> OBJECT = getClassInfo(Object.class);

    // Java
    public static ClassInfo<Number> NUMBER = getClassInfo(Number.class);
    public static ClassInfo<Long> LONG = getClassInfo(Long.class);
    public static ClassInfo<Boolean> BOOLEAN = getClassInfo(Boolean.class);
    public static ClassInfo<String> STRING = getClassInfo(String.class);

    // Bukkit
    public static ClassInfo<GameProfile> OFFLINE_PLAYER = getClassInfo(GameProfile.class);
    public static ClassInfo<FabricLocation> LOCATION = getClassInfo(FabricLocation.class);
    public static ClassInfo<Vec3> VECTOR = getClassInfo(Vec3.class);
    public static ClassInfo<ServerPlayer> PLAYER = getClassInfo(ServerPlayer.class);
    public static ClassInfo<ServerLevel> WORLD = getClassInfo(ServerLevel.class);

    // Skript
    public static ClassInfo<Color> COLOR = getClassInfo(Color.class);
    public static ClassInfo<Date> DATE = getClassInfo(Date.class);
    public static ClassInfo<Timespan> TIMESPAN = getClassInfo(Timespan.class);

    @NotNull
    private static <T> ClassInfo<T> getClassInfo(Class<T> type) {
        ClassInfo<T> classInfo = Classes.getExactClassInfo(type);
        if (classInfo == null) {
            throw new NullPointerException();
        }
        return classInfo;
    }
}
