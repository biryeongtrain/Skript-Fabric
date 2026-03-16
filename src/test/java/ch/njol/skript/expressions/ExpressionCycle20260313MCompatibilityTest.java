package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260313MCompatibilityTest {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        EntityData.register();
        SkriptFabricBootstrap.bootstrap();
        ensureSupportRegistered();
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsCycle20260313mExpressions() {
        assertInstanceOf(ExprSkull.class, parseExpression("skull of lane-m-offlineplayer", FabricItemType.class));
        assertInstanceOf(ExprSignText.class, parseExpression("line 2 of lane-m-sign-block", String.class));
        assertInstanceOf(ExprSignText.class, parseExpression("second line of lane-m-sign-block", String.class));
        @SuppressWarnings("unchecked")
        Class<EntityData<?>> entityDataType = (Class<EntityData<?>>) (Class<?>) EntityData.class;
        assertInstanceOf(ExprSpawnerType.class, parseExpression("spawner type of lane-m-spawner-block", entityDataType));
    }

    @Test
    void skullExpressionCreatesProfiledPlayerHead() {
        ExprSkull expression = new ExprSkull();
        FabricItemType itemType = expression.convert(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000c1"), "cyclem"));
        assertNotNull(itemType);
        assertEquals(Items.PLAYER_HEAD, itemType.item());
        ResolvableProfile profile = itemType.toStack().get(DataComponents.PROFILE);
        assertNotNull(profile);
        assertEquals("cyclem", profile.gameProfile().getName());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-0000000000c1"), profile.gameProfile().getId());
    }

    @Test
    void signAndSpawnerChangeContractsExposeExpectedMutations() {
        ExprSignText signText = new ExprSignText();
        assertArrayEquals(new Class[]{String.class}, signText.acceptChange(ChangeMode.SET));
        assertArrayEquals(new Class[]{String.class}, signText.acceptChange(ChangeMode.DELETE));

        ExprSpawnerType spawnerType = new ExprSpawnerType();
        assertArrayEquals(new Class[]{EntityData.class}, spawnerType.acceptChange(ChangeMode.SET));
        assertArrayEquals(new Class[]{EntityData.class}, spawnerType.acceptChange(ChangeMode.RESET));
        assertTrue(EntityData.parse("cow").matches(EntityType.COW));
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerExpression(LaneMOfflinePlayerExpression.class, GameProfile.class, "lane-m-offlineplayer");
        Skript.registerExpression(LaneMSignBlockExpression.class, FabricBlock.class, "lane-m-sign-block");
        Skript.registerExpression(LaneMSpawnerBlockExpression.class, FabricBlock.class, "lane-m-spawner-block");
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    public static final class LaneMOfflinePlayerExpression extends SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return new GameProfile[]{new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000c1"), "cyclem")};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends GameProfile> getReturnType() {
            return GameProfile.class;
        }
    }

    public static final class LaneMSignBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }
    }

    public static final class LaneMSpawnerBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }
    }
}
