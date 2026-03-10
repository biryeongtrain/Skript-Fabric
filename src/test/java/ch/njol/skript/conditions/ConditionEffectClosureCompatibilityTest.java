package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.effects.EffFeed;
import ch.njol.skript.effects.EffInvisible;
import ch.njol.skript.effects.EffInvulnerability;
import ch.njol.skript.effects.EffKill;
import ch.njol.skript.effects.EffSilence;
import ch.njol.skript.effects.EffSprinting;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ConditionEffectClosureCompatibilityTest {

    @Test
    void compareSupportsNumericAliasesAndNegation() {
        CondCompare equal = new CondCompare();
        equal.init(new Expression[]{
                new SimpleLiteral<>(1, false),
                new SimpleLiteral<>("1", false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(equal.check(SkriptEvent.EMPTY));

        CondCompare negated = new CondCompare();
        negated.init(new Expression[]{
                new SimpleLiteral<>(1, false),
                new SimpleLiteral<>("1", false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertFalse(negated.check(SkriptEvent.EMPTY));
    }

    @Test
    void importedConditionsAndEffectsInstantiate() {
        assertDoesNotThrow(CondAI::new);
        assertDoesNotThrow(CondCompare::new);
        assertDoesNotThrow(CondIsAlive::new);
        assertDoesNotThrow(CondIsBurning::new);
        assertDoesNotThrow(CondIsEmpty::new);
        assertDoesNotThrow(CondIsInvisible::new);
        assertDoesNotThrow(CondIsInvulnerable::new);
        assertDoesNotThrow(CondIsSilent::new);
        assertDoesNotThrow(CondIsSprinting::new);
        assertDoesNotThrow(EffFeed::new);
        assertDoesNotThrow(EffInvisible::new);
        assertDoesNotThrow(EffInvulnerability::new);
        assertDoesNotThrow(EffKill::new);
        assertDoesNotThrow(EffSilence::new);
        assertDoesNotThrow(EffSprinting::new);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
