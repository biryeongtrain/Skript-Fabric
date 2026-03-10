package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ConditionValueCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void divisibleBySupportsToleranceAndNegation() {
        CondIsDivisibleBy divisible = new CondIsDivisibleBy();
        divisible.init(new Expression[]{
                new SimpleLiteral<>(0.3, false),
                new SimpleLiteral<>(0.1, false),
                new SimpleLiteral<>(0.0000001, false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(divisible.check(SkriptEvent.EMPTY));

        CondIsDivisibleBy negated = new CondIsDivisibleBy();
        negated.init(new Expression[]{
                new SimpleLiteral<>(11, false),
                new SimpleLiteral<>(10, false),
                new SimpleLiteral<>(0.0000001, false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(negated.check(SkriptEvent.EMPTY));
    }

    @Test
    void minecraftVersionSimplifiesForLiteralChecks() {
        CondMinecraftVersion currentOrNewer = new CondMinecraftVersion();
        currentOrNewer.init(new Expression[]{new SimpleLiteral<>("1.20", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(currentOrNewer.check(SkriptEvent.EMPTY));

        CondMinecraftVersion belowFuture = new CondMinecraftVersion();
        SkriptParser.ParseResult belowParse = parseResult("");
        belowParse.mark = 1;
        belowFuture.init(new Expression[]{new SimpleLiteral<>("99.0", false)}, 0, Kleenean.FALSE, belowParse);
        assertTrue(belowFuture.check(SkriptEvent.EMPTY));
    }

    @Test
    void usingFeatureChecksScriptExperimentSet() {
        Script script = new Script(new Config("lane-e", "lane-e.sk", null), List.of());
        script.addData(new ExperimentSet(List.of(Feature.EXAMPLES, Feature.QUEUES)));

        CondIsUsingFeature using = new CondIsUsingFeature();
        using.init(new Expression[]{
                new SimpleLiteral<>(script, false),
                new SimpleLiteral<>("examples", false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(using.check(SkriptEvent.EMPTY));

        CondIsUsingFeature negated = new CondIsUsingFeature();
        negated.init(new Expression[]{
                new SimpleLiteral<>(script, false),
                new SimpleLiteral<>("reflection", false)
        }, 2, Kleenean.FALSE, parseResult(""));
        assertTrue(negated.check(SkriptEvent.EMPTY));
        assertFalse(using.isNegated());
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
