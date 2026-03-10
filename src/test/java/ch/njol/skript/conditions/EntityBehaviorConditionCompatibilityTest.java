package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class EntityBehaviorConditionCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void importedEntityBehaviorConditionsInstantiate() {
        assertDoesNotThrow(CondAllayCanDuplicate::new);
        assertDoesNotThrow(CondEntityIsInLiquid::new);
        assertDoesNotThrow(CondEntityIsWet::new);
        assertDoesNotThrow(CondGoatHasHorns::new);
        assertDoesNotThrow(CondIsChargingFireball::new);
        assertDoesNotThrow(CondIsCustomNameVisible::new);
        assertDoesNotThrow(CondIsDashing::new);
        assertDoesNotThrow(CondIsFrozen::new);
        assertDoesNotThrow(CondIsPlayingDead::new);
        assertDoesNotThrow(CondIsScreaming::new);
        assertDoesNotThrow(CondIsSheared::new);
        assertDoesNotThrow(CondPandaIsOnBack::new);
        assertDoesNotThrow(CondPandaIsRolling::new);
        assertDoesNotThrow(CondPandaIsScared::new);
        assertDoesNotThrow(CondPandaIsSneezing::new);
        assertDoesNotThrow(CondStriderIsShivering::new);
    }

    @Test
    void propertyConditionsPreserveLegacyToStringShapes() {
        CondAllayCanDuplicate canDuplicate = new CondAllayCanDuplicate();
        canDuplicate.init(new Expression[]{new TestExpression<>("allays", LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("allays can duplicate", canDuplicate.toString(SkriptEvent.EMPTY, false));

        CondGoatHasHorns rightHorn = new CondGoatHasHorns();
        SkriptParser.ParseResult rightParse = parseResult("");
        rightParse.tags.add("right");
        rightHorn.init(new Expression[]{new TestExpression<>("goat", LivingEntity.class)}, 0, Kleenean.FALSE, rightParse);
        assertEquals("goat has right horn", rightHorn.toString(SkriptEvent.EMPTY, false));

        CondEntityIsWet wet = new CondEntityIsWet();
        wet.init(new Expression[]{new TestExpression<>("entities", Entity.class)}, 1, Kleenean.FALSE, parseResult(""));
        assertEquals("entities are not wet", wet.toString(SkriptEvent.EMPTY, false));

        CondPandaIsOnBack onBack = new CondPandaIsOnBack();
        onBack.init(new Expression[]{new TestExpression<>("panda", LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("panda is on their back", onBack.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void customSyntaxConditionsRetainExpectedTextForms() {
        CondEntityIsInLiquid inBubbleColumn = new CondEntityIsInLiquid();
        SkriptParser.ParseResult bubbleParse = parseResult("");
        bubbleParse.mark = 3;
        inBubbleColumn.init(new Expression[]{new TestExpression<>("player", Entity.class)}, 0, Kleenean.FALSE, bubbleParse);
        assertEquals("player is in bubble column", inBubbleColumn.toString(SkriptEvent.EMPTY, false));

        CondEntityIsInLiquid notInRain = new CondEntityIsInLiquid();
        SkriptParser.ParseResult rainParse = parseResult("");
        rainParse.mark = 4;
        notInRain.init(new Expression[]{new TestExpression<>("players", Entity.class)}, 1, Kleenean.FALSE, rainParse);
        assertEquals("players are not in rain", notInRain.toString(SkriptEvent.EMPTY, false));

        CondIsCustomNameVisible customNameVisible = new CondIsCustomNameVisible();
        customNameVisible.init(new Expression[]{new TestExpression<>("target", Entity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("target is custom name", customNameVisible.toString(SkriptEvent.EMPTY, false));

        CondIsCustomNameVisible customNameHidden = new CondIsCustomNameVisible();
        customNameHidden.init(new Expression[]{new TestExpression<>("targets", Entity.class)}, 3, Kleenean.FALSE, parseResult(""));
        assertEquals("targets are not custom name", customNameHidden.toString(SkriptEvent.EMPTY, false));
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
