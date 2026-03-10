package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class EntityStateConditionCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void importedEntityStateConditionsInstantiate() {
        assertDoesNotThrow(CondCanFly::new);
        assertDoesNotThrow(CondCanPickUpItems::new);
        assertDoesNotThrow(CondHasScoreboardTag::new);
        assertDoesNotThrow(CondIsBlocking::new);
        assertDoesNotThrow(CondIsClimbing::new);
        assertDoesNotThrow(CondIsFlying::new);
        assertDoesNotThrow(CondIsGliding::new);
        assertDoesNotThrow(CondIsHandRaised::new);
        assertDoesNotThrow(CondIsLeftHanded::new);
        assertDoesNotThrow(CondIsOnGround::new);
        assertDoesNotThrow(CondIsRiptiding::new);
        assertDoesNotThrow(CondIsSleeping::new);
        assertDoesNotThrow(CondIsSneaking::new);
        assertDoesNotThrow(CondIsSwimming::new);
        assertDoesNotThrow(CondIsTamed::new);
    }

    @Test
    void propertyConditionsPreserveLegacyToStringShapes() {
        CondCanFly canFly = new CondCanFly();
        canFly.init(new Expression[]{new TestExpression<>("players", ServerPlayer.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("players can fly", canFly.toString(SkriptEvent.EMPTY, false));

        CondCanPickUpItems pickUpItems = new CondCanPickUpItems();
        pickUpItems.init(new Expression[]{new TestExpression<>("mobs", LivingEntity.class)}, 1, Kleenean.FALSE, parseResult(""));
        assertEquals("mobs can't pick up items", pickUpItems.toString(SkriptEvent.EMPTY, false));

        CondIsBlocking blocking = new CondIsBlocking();
        blocking.init(new Expression[]{new TestExpression<>("entity", LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("entity is blocking", blocking.toString(SkriptEvent.EMPTY, false));

        CondIsFlying flying = new CondIsFlying();
        flying.init(new Expression[]{new TestExpression<>("player", ServerPlayer.class)}, 1, Kleenean.FALSE, parseResult(""));
        assertEquals("player is not flying", flying.toString(SkriptEvent.EMPTY, false));

        CondIsLeftHanded leftHanded = new CondIsLeftHanded();
        SkriptParser.ParseResult leftParse = parseResult("");
        leftParse.tags.add("left");
        leftHanded.init(new Expression[]{new TestExpression<>("entity", LivingEntity.class)}, 0, Kleenean.FALSE, leftParse);
        assertEquals("entity is left handed", leftHanded.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void scoreboardTagAndHandRaisedRetainCustomTextForms() {
        CondHasScoreboardTag scoreboardTag = new CondHasScoreboardTag();
        scoreboardTag.init(
                new Expression[]{
                        new TestExpression<>("entities", Entity.class),
                        new SimpleLiteral<>("raid_target", false)
                },
                0,
                Kleenean.FALSE,
                parseResult("")
        );
        assertEquals("entities have the scoreboard tag [raid_target]", scoreboardTag.toString(SkriptEvent.EMPTY, false));

        CondIsHandRaised mainHandRaised = new CondIsHandRaised();
        SkriptParser.ParseResult mainParse = parseResult("");
        mainParse.tags.add("main");
        mainHandRaised.init(new Expression[]{new TestExpression<>("player", LivingEntity.class)}, 0, Kleenean.FALSE, mainParse);
        assertEquals("player's main hand is raised", mainHandRaised.toString(SkriptEvent.EMPTY, false));

        CondIsHandRaised offHandRaised = new CondIsHandRaised();
        offHandRaised.init(new Expression[]{new TestExpression<>("players", LivingEntity.class)}, 5, Kleenean.FALSE, parseResult(""));
        assertEquals("players's off hands are not raised", offHandRaised.toString(SkriptEvent.EMPTY, false));
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
