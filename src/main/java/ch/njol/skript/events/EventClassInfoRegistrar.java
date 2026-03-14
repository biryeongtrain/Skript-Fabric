package ch.njol.skript.events;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

final class EventClassInfoRegistrar {

    private static boolean registered;

    private EventClassInfoRegistrar() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        EntityData.register();
        registerEnum(GameType.class, "gamemode", "game modes");
        registerEnum(FabricEventCompatHandles.WeatherType.class, "weathertype", "weather types");
        registerEnum(FabricEventCompatHandles.ArmorSlot.class, "armorslot", "armor slots");
        registerEnum(FabricEventCompatHandles.ResourcePackState.class, "resourcepackstate", "resource pack states");
        registerEnum(SpawnReason.class, "spawnreason", "spawn reasons");
        registerEnum(TeleportCause.class, "teleportcause", "teleport causes");
        registerEnum(DyeColor.class, "dyecolor", "dye colors");
        registerBannerPatternLayer();
        registered = true;
    }

    private static <T extends Enum<T>> void registerEnum(Class<T> type, String codeName, String languageNode) {
        if (Classes.getExactClassInfo(type) != null) {
            return;
        }
        Classes.registerClassInfo(new EnumClassInfo<>(type, codeName, languageNode));
    }

    private static void registerBannerPatternLayer() {
        if (Classes.getExactClassInfo(BannerPatternLayers.Layer.class) != null)
            return;
        Classes.registerClassInfo(new ClassInfo<>(BannerPatternLayers.Layer.class, "bannerpattern")
            .name("Banner Pattern")
            .description("A banner pattern layer consisting of a pattern type and a dye color.")
            .parser(new ClassInfo.Parser<>() {
                @Override
                public boolean canParse(ParseContext context) {
                    return false;
                }

                @Override
                public @Nullable BannerPatternLayers.Layer parse(String input, ParseContext context) {
                    return null;
                }
            }));
    }
}
