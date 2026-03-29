package ch.njol.skript.conditions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;
import net.minecraft.world.phys.Vec3;

class ConditionBlockItemCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
    }

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void itemPropertyConditionsHandleBlockFoodFuelAndVisibilityProperties() {
        assertTrue(checkProperty(new CondIsBlock(), item(Items.STONE), 0));
        assertTrue(checkProperty(new CondIsEdible(), item(Items.COOKED_BEEF), 0));
        assertTrue(checkProperty(new CondIsFlammable(), item(Items.OAK_PLANKS), 0));
        assertTrue(checkProperty(new CondIsInteractable(), item(Items.LEVER), 0));
        assertTrue(checkProperty(new CondIsOccluding(), item(Items.STONE), 0));
        assertTrue(checkProperty(new CondIsSolid(), item(Items.STONE), 0));
        assertTrue(checkProperty(new CondIsTransparent(), item(Items.GLASS), 0));

        CondIsBlock negated = new CondIsBlock();
        negated.init(new Expression[]{literal(item(Items.DIAMOND_SWORD))}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(negated.check(SkriptEvent.EMPTY));
        assertTrue(negated.toString(SkriptEvent.EMPTY, false).endsWith("is not block"));
    }

    @Test
    void infiniteAndVectorConditionsSimplifyLiteralChecks() {
        CondIsInfinite timespanCondition = new CondIsInfinite();
        timespanCondition.init(new Expression[]{literal(Timespan.parse("infinite"))}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(timespanCondition.check(SkriptEvent.EMPTY));

        CondIsVectorNormalized normalized = new CondIsVectorNormalized();
        normalized.init(new Expression[]{literal(new Vec3(1, 0, 0))}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(normalized.check(SkriptEvent.EMPTY));

        CondIsVectorNormalized negated = new CondIsVectorNormalized();
        negated.init(new Expression[]{literal(new Vec3(2, 0, 0))}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(negated.check(SkriptEvent.EMPTY));
    }

    private static boolean checkProperty(ch.njol.skript.conditions.base.PropertyCondition<FabricItemType> condition, FabricItemType item, int matchedPattern) {
        condition.init(new Expression[]{literal(item)}, matchedPattern, Kleenean.FALSE, parseResult(""));
        return condition.check(SkriptEvent.EMPTY);
    }

    private static FabricItemType item(net.minecraft.world.item.Item item) {
        return new FabricItemType(item);
    }

    private static <T> SimpleLiteral<T> literal(T value) {
        return new SimpleLiteral<>(value, false);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
