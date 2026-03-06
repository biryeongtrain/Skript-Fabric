package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.Skript;
import org.skriptlang.skript.bukkit.base.types.BlockClassInfo;
import org.skriptlang.skript.bukkit.base.types.EntityClassInfo;
import org.skriptlang.skript.bukkit.base.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemStackClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemTypeClassInfo;
import org.skriptlang.skript.bukkit.base.types.LocationClassInfo;
import org.skriptlang.skript.bukkit.base.types.NameableClassInfo;
import org.skriptlang.skript.bukkit.base.types.OfflinePlayerClassInfo;
import org.skriptlang.skript.bukkit.base.types.PlayerClassInfo;
import org.skriptlang.skript.bukkit.base.types.SlotClassInfo;
import org.skriptlang.skript.bukkit.base.types.VectorClassInfo;
import org.skriptlang.skript.bukkit.base.types.WorldClassInfo;
import org.skriptlang.skript.fabric.SkriptFabric;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.syntax.effect.EffSetTestBlockAtLocation;
import org.skriptlang.skript.fabric.syntax.effect.EffSetTestBlockAtBlock;
import org.skriptlang.skript.fabric.syntax.effect.EffSetTestBlock;
import org.skriptlang.skript.fabric.syntax.effect.EffSetTestEntityName;
import org.skriptlang.skript.fabric.syntax.effect.EffSetTestBlockUnderPlayer;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricBlockBreak;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricGameTest;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricServerTick;
import org.skriptlang.skript.fabric.syntax.event.EvtFabricUseBlock;
import org.skriptlang.skript.fabric.syntax.event.EvtUseEntity;
import org.skriptlang.skript.fabric.syntax.expression.ExprEventBlock;
import org.skriptlang.skript.fabric.syntax.expression.ExprEventEntity;
import org.skriptlang.skript.fabric.syntax.expression.ExprEventPlayer;
import org.skriptlang.skript.lang.properties.Property;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class SkriptFabricBootstrap {

    private static volatile boolean bootstrapped;

    private SkriptFabricBootstrap() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        synchronized (SkriptFabricBootstrap.class) {
            if (bootstrapped) {
                return;
            }

            Skript.setAcceptRegistrations(true);
            try {
                Property.registerDefaultProperties();
                PlayerClassInfo.register();
                InventoryClassInfo.register();
                ItemStackClassInfo.register();
                ItemTypeClassInfo.register();
                LocationClassInfo.register();
                NameableClassInfo.register();
                OfflinePlayerClassInfo.register();
                WorldClassInfo.register();
                EntityClassInfo.register();
                BlockClassInfo.register();
                SlotClassInfo.register();
                VectorClassInfo.register();
                SkriptFabricEventBridge.register();
                Skript.registerEvent(EvtFabricBlockBreak.class, "on block break");
                Skript.registerEvent(EvtFabricGameTest.class, "on gametest");
                Skript.registerEvent(EvtFabricServerTick.class, "on server tick");
                Skript.registerEvent(EvtFabricUseBlock.class, "on use block");
                Skript.registerEvent(EvtUseEntity.class, "on use entity");
                Skript.registerExpression(
                        ExprEventBlock.class,
                        FabricBlock.class,
                        "event-block",
                        "event block",
                        "the event-block",
                        "the event block"
                );
                Skript.registerExpression(
                        ExprEventPlayer.class,
                        ServerPlayer.class,
                        "event-player",
                        "event player",
                        "the event-player",
                        "the event player"
                );
                Skript.registerExpression(
                        ExprEventEntity.class,
                        Entity.class,
                        "event-entity",
                        "event entity",
                        "the event-entity",
                        "the event entity"
                );
                Skript.registerEffect(
                        EffSetTestBlock.class,
                        "set test block at %integer% %integer% %integer% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestBlockAtBlock.class,
                        "set test block for %block% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestBlockAtLocation.class,
                        "set test block at %location% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestBlockUnderPlayer.class,
                        "set test block under player %player% to %string%"
                );
                Skript.registerEffect(
                        EffSetTestEntityName.class,
                        "set test name of entity %entity% to %string%"
                );
            } finally {
                Skript.setAcceptRegistrations(false);
            }

            bootstrapped = true;
            SkriptFabric.LOGGER.info("Initialized minimal Skript Fabric runtime bootstrap.");
        }
    }
}
