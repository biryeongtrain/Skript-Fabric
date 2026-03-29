package ch.njol.skript.conditions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ConditionSurfaceEConditionCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
    }

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void surfaceEConditionsInstantiate() {
        assertDoesNotThrow(CondCanSee::new);
        assertDoesNotThrow(CondGlowingText::new);
        assertDoesNotThrow(CondIsLoaded::new);
        assertDoesNotThrow(CondIsPathfinding::new);
        assertDoesNotThrow(CondIsRiding::new);
        assertDoesNotThrow(CondIsRinging::new);
        assertDoesNotThrow(CondIsSaddled::new);
        assertDoesNotThrow(CondPlayedBefore::new);
        assertDoesNotThrow(CondTooltip::new);
        assertDoesNotThrow(CondCanHold::new);
        assertDoesNotThrow(CondIsStackable::new);
        assertDoesNotThrow(CondWithinRadius::new);
        assertDoesNotThrow(CondIsWithin::new);
    }

    @Test
    void surfaceEConditionsPreserveLegacyToStringShapes() {
        CondCanSee canSee = new CondCanSee();
        canSee.init(new Expression[]{
                new TestExpression<>("player", net.minecraft.server.level.ServerPlayer.class),
                new TestExpression<>("entities", net.minecraft.world.entity.Entity.class)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertEquals("player can see entities", canSee.toString(SkriptEvent.EMPTY, false));

        CondIsLoaded loaded = new CondIsLoaded();
        loaded.init(new Expression[]{new TestExpression<>("scripts", org.skriptlang.skript.lang.script.Script.class)}, 2, Kleenean.FALSE, parseResult(""));
        assertEquals("scripts scripts are loaded", loaded.toString(SkriptEvent.EMPTY, false));

        CondIsPathfinding pathfinding = new CondIsPathfinding();
        pathfinding.init(new Expression[]{
                new TestExpression<>("mob", net.minecraft.world.entity.LivingEntity.class),
                new TestExpression<>("target", Object.class)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("mob is pathfinding to target", pathfinding.toString(SkriptEvent.EMPTY, false));

        CondIsRiding riding = new CondIsRiding();
        riding.init(new Expression[]{new TestExpression<>("entity", net.minecraft.world.entity.Entity.class), null}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("entity is riding", riding.toString(SkriptEvent.EMPTY, false));

        CondPlayedBefore playedBefore = new CondPlayedBefore();
        playedBefore.init(new Expression[]{new TestExpression<>("players", com.mojang.authlib.GameProfile.class)}, 1, Kleenean.FALSE, parseResult(""));
        assertEquals("players haven't played on this server before", playedBefore.toString(SkriptEvent.EMPTY, false));

        CondTooltip tooltip = new CondTooltip();
        SkriptParser.ParseResult tooltipParse = parseResult("");
        tooltipParse.tags.add("additional");
        tooltipParse.tags.add("hidden");
        tooltip.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STICK), false)}, 0, Kleenean.FALSE, tooltipParse);
        assertEquals("the additional tooltip of [stick] is shown", tooltip.toString(SkriptEvent.EMPTY, false));

        CondCanHold canHold = new CondCanHold();
        canHold.init(new Expression[]{
                new TestExpression<>("inventory", FabricInventory.class),
                new TestExpression<>("items", FabricItemType.class)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("inventory can hold items", canHold.toString(SkriptEvent.EMPTY, false));

        CondWithinRadius withinRadius = new CondWithinRadius();
        withinRadius.init(new Expression[]{
                new TestExpression<>("locations", FabricLocation.class),
                new TestExpression<>("10", Number.class),
                new TestExpression<>("center", FabricLocation.class)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("locations are within 10 blocks around center", withinRadius.toString(SkriptEvent.EMPTY, false));

        CondIsWithin within = new CondIsWithin();
        within.init(new Expression[]{
                new TestExpression<>("location", FabricLocation.class),
                new TestExpression<>("corner1", FabricLocation.class),
                new TestExpression<>("corner2", FabricLocation.class)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("location is within corner1 and corner2", within.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void surfaceERuntimeChecksCoverFuelTooltipsHoldingAndStacking() {
        CondIsStackable stackable = new CondIsStackable();
        stackable.init(new Expression[]{new SimpleLiteral<>(new ItemStack(Items.DIRT), false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(stackable.check(SkriptEvent.EMPTY));

        CondIsStackable notStackable = new CondIsStackable();
        notStackable.init(new Expression[]{new SimpleLiteral<>(new ItemStack(Items.DIAMOND_SWORD), false)}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(notStackable.check(SkriptEvent.EMPTY));

        SimpleContainer container = new SimpleContainer(1);
        CondCanHold canHold = new CondCanHold();
        canHold.init(new Expression[]{
                new SimpleLiteral<>(new FabricInventory(container), false),
                new SimpleLiteral<>(new FabricItemType(Items.DIRT, 16, null), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(canHold.check(SkriptEvent.EMPTY));

        container.setItem(0, new ItemStack(Items.DIRT, 64));
        CondCanHold full = new CondCanHold();
        full.init(new Expression[]{
                new SimpleLiteral<>(new FabricInventory(container), false),
                new SimpleLiteral<>(new FabricItemType(Items.DIRT, 1, null), false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(full.check(SkriptEvent.EMPTY));

        FabricLocation center = new FabricLocation(null, new net.minecraft.world.phys.Vec3(0, 64, 0));
        FabricLocation near = new FabricLocation(null, new net.minecraft.world.phys.Vec3(3, 64, 4));
        CondWithinRadius radius = new CondWithinRadius();
        radius.init(new Expression[]{
                new SimpleLiteral<>(near, false),
                new SimpleLiteral<>(5, false),
                new SimpleLiteral<>(center, false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(radius.check(SkriptEvent.EMPTY));

        CondWithinRadius tooFar = new CondWithinRadius();
        tooFar.init(new Expression[]{
                new SimpleLiteral<>(near, false),
                new SimpleLiteral<>(4, false),
                new SimpleLiteral<>(center, false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(tooFar.check(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static final class TestExpression<T> extends SimpleExpression<T> {

        private final String text;
        private final Class<? extends T> returnType;

        private TestExpression(String text, Class<? extends T> returnType) {
            this.text = text;
            this.returnType = returnType;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            @SuppressWarnings("unchecked")
            T[] empty = (T[]) java.lang.reflect.Array.newInstance(returnType, 0);
            return empty;
        }

        @Override
        public boolean isSingle() {
            return !text.endsWith("s");
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return text;
        }
    }
}
