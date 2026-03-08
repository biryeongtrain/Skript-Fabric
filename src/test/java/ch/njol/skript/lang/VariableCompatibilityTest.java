package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.Variables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

class VariableCompatibilityTest {

    @AfterEach
    void cleanupVariables() {
        Variables.clearAll();
        Variables.caseInsensitiveVariables = true;
        Skript.instance().syntaxRegistry().clearAll();
        ParserInstance.get().setCurrentScript(null);
    }

    @Test
    void validatesVariableNameRules() {
        assertTrue(Variable.isValidVariableName("health", true, false));
        assertTrue(Variable.isValidVariableName("data::*", true, false));
        assertFalse(Variable.isValidVariableName("::bad", true, false));
        assertFalse(Variable.isValidVariableName("bad::", true, false));
        assertFalse(Variable.isValidVariableName("bad:*", true, false));
    }

    @Test
    void newInstanceResolvesLocalAndListFlags() {
        Variable<Integer> variable = Variable.newInstance("_stats::*", new Class[]{Integer.class});

        assertNotNull(variable);
        assertTrue(variable.isLocal());
        assertTrue(variable.isList());
        assertFalse(variable.isSingle());
    }

    @Test
    void localVariablesUseKnownHintsToNarrowGenericObjectType() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        parser.getHintManager().enterScope(true);
        parser.getHintManager().set("value", Integer.class);

        Variable<Object> variable = Variable.newInstance("_value", new Class[]{Object.class});

        assertNotNull(variable);
        assertEquals(Integer.class, variable.getReturnType());
    }

    @Test
    void localVariablesRejectIncompatibleKnownHints() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        parser.getHintManager().enterScope(true);
        parser.getHintManager().set("value", Integer.class);

        Variable<String> variable = Variable.newInstance("_value", new Class[]{String.class});

        assertNull(variable);
    }

    @Test
    void singleVariableSupportsSetAndAdd() {
        Variable<Number> variable = Variable.newInstance("count", new Class[]{Number.class});
        assertNotNull(variable);

        variable.change(SkriptEvent.EMPTY, new Object[]{5}, ChangeMode.SET);
        assertEquals(5, variable.getSingle(SkriptEvent.EMPTY));

        variable.change(SkriptEvent.EMPTY, new Object[]{3}, ChangeMode.ADD);
        assertEquals(8L, variable.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void listVariableExposesValuesAndKeys() {
        Variable<Integer> variable = Variable.newInstance("scores::*", new Class[]{Integer.class});
        assertNotNull(variable);

        variable.change(SkriptEvent.EMPTY, new Object[]{1, 2}, ChangeMode.SET);
        variable.change(SkriptEvent.EMPTY, new Object[]{9}, ChangeMode.SET, new String[]{"foo"});

        assertArrayEquals(new Integer[]{1, 2, 9}, variable.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"1", "2", "foo"}, variable.getArrayKeys(SkriptEvent.EMPTY));

        variable.change(SkriptEvent.EMPTY, new Object[]{0}, ChangeMode.DELETE, new String[]{"foo"});
        assertArrayEquals(new String[]{"1", "2"}, variable.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void listVariablesDoNotRecommendPreservingKeysOnSet() {
        Variable<String> variable = Variable.newInstance("scores::*", new Class[]{String.class});
        assertNotNull(variable);

        assertFalse(variable.areKeysRecommended());
    }

    @Test
    void listVariablesExposeNumericKeysInNaturalOrder() {
        Variable<String> variable = Variable.newInstance("scores::*", new Class[]{String.class});
        assertNotNull(variable);

        variable.change(SkriptEvent.EMPTY, new Object[]{"emerald_block"}, ChangeMode.SET, new String[]{"10"});
        variable.change(SkriptEvent.EMPTY, new Object[]{"gold_block"}, ChangeMode.SET, new String[]{"2"});

        assertArrayEquals(new String[]{"gold_block", "emerald_block"}, variable.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"2", "10"}, variable.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void listVariablesSkipNestedDescendantEntriesWithoutDirectParentValues() {
        Variable<String> nested = Variable.newInstance("scores::group::1", new Class[]{String.class});
        Variable<String> plain = Variable.newInstance("scores::plain", new Class[]{String.class});
        Variable<String> source = Variable.newInstance("scores::*", new Class[]{String.class});
        assertNotNull(nested);
        assertNotNull(plain);
        assertNotNull(source);

        nested.change(SkriptEvent.EMPTY, new Object[]{"gold_block"}, ChangeMode.SET);
        plain.change(SkriptEvent.EMPTY, new Object[]{"emerald_block"}, ChangeMode.SET);

        assertArrayEquals(new String[]{"emerald_block"}, source.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"plain"}, source.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void listVariablesExposeLegacyLoopAliases() {
        Variable<String> variable = Variable.newInstance("scores::*", new Class[]{String.class});
        assertNotNull(variable);

        assertTrue(variable.isLoopOf("index"));
        assertTrue(variable.isLoopOf("var"));
        assertTrue(variable.isLoopOf("variable"));
        assertTrue(variable.isLoopOf("value"));
        assertFalse(variable.isLoopOf("player"));
    }

    @Test
    void listVariableChecksRequireAllValuesToMatchWhenAndSemanticsApply() {
        Variable<Integer> variable = Variable.newInstance("scores::*", new Class[]{Integer.class});
        assertNotNull(variable);

        variable.change(SkriptEvent.EMPTY, new Object[]{2, 4, 6}, ChangeMode.SET);

        assertTrue(variable.check(SkriptEvent.EMPTY, value -> value % 2 == 0));
        assertFalse(variable.check(SkriptEvent.EMPTY, value -> value >= 4));
        assertTrue(variable.getAnd());
    }

    @Test
    void localVariablesAreScopedByEventHandle() {
        Variable<String> variable = Variable.newInstance("_session", new Class[]{String.class});
        assertNotNull(variable);

        SkriptEvent first = new SkriptEvent(new Object(), null, null, null);
        SkriptEvent second = new SkriptEvent(new Object(), null, null, null);

        variable.change(first, new Object[]{"alpha"}, ChangeMode.SET);

        assertEquals("alpha", variable.getSingle(first));
        assertNull(variable.getSingle(second));
    }

    @Test
    void withLocalVariablesCopiesBackProviderChangesAndClearsTargetScope() {
        Variable<String> state = Variable.newInstance("_state", new Class[]{String.class});
        Variable<String> created = Variable.newInstance("_created", new Class[]{String.class});
        assertNotNull(state);
        assertNotNull(created);

        SkriptEvent provider = new SkriptEvent(new Object(), null, null, null);
        SkriptEvent user = new SkriptEvent(new Object(), null, null, null);

        state.change(provider, new Object[]{"outer"}, ChangeMode.SET);
        state.change(user, new Object[]{"stale"}, ChangeMode.SET);

        Variables.withLocalVariables(provider, user, () -> {
            assertEquals("outer", state.getSingle(user));
            state.change(user, new Object[]{"inner"}, ChangeMode.SET);
            created.change(user, new Object[]{"created-inside"}, ChangeMode.SET);
        });

        assertEquals("inner", state.getSingle(provider));
        assertEquals("created-inside", created.getSingle(provider));
        assertNull(state.getSingle(user));
        assertNull(created.getSingle(user));
    }

    @Test
    void parserRecognizesVariableExpressions() {
        Expression<?> parsed = new SkriptParser("{MiXeD}", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{String.class});

        assertNotNull(parsed);
        assertInstanceOf(Variable.class, parsed);
        assertEquals("{MiXeD}", parsed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void parserRecognizesPrefixedVariableExpressions() {
        Expression<?> shortPrefixed = new SkriptParser("var {MiXeD}", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{String.class});
        Expression<?> longPrefixed = new SkriptParser("the variable {MiXeD}", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{String.class});

        assertNotNull(shortPrefixed);
        assertInstanceOf(Variable.class, shortPrefixed);
        assertEquals("{MiXeD}", shortPrefixed.toString(SkriptEvent.EMPTY, false));

        assertNotNull(longPrefixed);
        assertInstanceOf(Variable.class, longPrefixed);
        assertEquals("{MiXeD}", longPrefixed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void variablesAreCaseInsensitiveByDefault() {
        Variable<String> mixed = Variable.newInstance("MiXeD", new Class[]{String.class});
        Variable<String> lower = Variable.newInstance("mixed", new Class[]{String.class});
        assertNotNull(mixed);
        assertNotNull(lower);

        mixed.change(SkriptEvent.EMPTY, new Object[]{"emerald_block"}, ChangeMode.SET);

        assertEquals("emerald_block", lower.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void variablesCanBeConfiguredCaseSensitive() {
        Variables.caseInsensitiveVariables = false;

        Variable<String> mixed = Variable.newInstance("MiXeD", new Class[]{String.class});
        Variable<String> lower = Variable.newInstance("mixed", new Class[]{String.class});
        assertNotNull(mixed);
        assertNotNull(lower);

        mixed.change(SkriptEvent.EMPTY, new Object[]{"emerald_block"}, ChangeMode.SET);

        assertNull(lower.getSingle(SkriptEvent.EMPTY));
        assertEquals("emerald_block", mixed.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parsedChangeEffectStoresVariableValue() {
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );

        Statement setVariable = Statement.parse("set {MiXeDBlock} to \"gold_block\"", "failed");
        Expression<?> lookup = new SkriptParser("{mixedblock}", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{String.class});

        assertNotNull(setVariable);
        assertNotNull(lookup);

        TriggerItem.walk(setVariable, SkriptEvent.EMPTY);

        assertEquals("gold_block", lookup.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parsedChangeEffectStoresPrefixedVariableValue() {
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );

        Statement setVariable = Statement.parse("set var {MiXeDBlock} to \"gold_block\"", "failed");
        Expression<?> lookup = new SkriptParser("the variable {mixedblock}", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{String.class});

        assertNotNull(setVariable);
        assertNotNull(lookup);

        TriggerItem.walk(setVariable, SkriptEvent.EMPTY);

        assertEquals("gold_block", lookup.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parsedListToListSetRenumbersInsteadOfPreservingSourceKeys() {
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );

        Statement setFoo = Statement.parse("set {source::foo} to \"gold_block\"", "failed");
        Statement setBar = Statement.parse("set {source::bar} to \"emerald_block\"", "failed");
        Statement copyList = Statement.parse("set {target::*} to {source::*}", "failed");
        Variable<String> first = Variable.newInstance("target::1", new Class[]{String.class});
        Variable<String> second = Variable.newInstance("target::2", new Class[]{String.class});
        Variable<String> foo = Variable.newInstance("target::foo", new Class[]{String.class});
        Variable<String> bar = Variable.newInstance("target::bar", new Class[]{String.class});

        assertNotNull(setFoo);
        assertNotNull(setBar);
        assertNotNull(copyList);
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(foo);
        assertNotNull(bar);

        TriggerItem.walk(setFoo, SkriptEvent.EMPTY);
        TriggerItem.walk(setBar, SkriptEvent.EMPTY);
        TriggerItem.walk(copyList, SkriptEvent.EMPTY);

        String firstValue = first.getSingle(SkriptEvent.EMPTY);
        String secondValue = second.getSingle(SkriptEvent.EMPTY);

        assertNotNull(firstValue);
        assertNotNull(secondValue);
        assertNotEquals(firstValue, secondValue);
        assertTrue(java.util.Set.of("gold_block", "emerald_block").contains(firstValue));
        assertTrue(java.util.Set.of("gold_block", "emerald_block").contains(secondValue));
        assertNull(foo.getSingle(SkriptEvent.EMPTY));
        assertNull(bar.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parsedListToListSetUsesNaturalNumericOrderingForSourceKeys() {
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );

        Statement setTen = Statement.parse("set {source::10} to \"emerald_block\"", "failed");
        Statement setTwo = Statement.parse("set {source::2} to \"gold_block\"", "failed");
        Statement copyList = Statement.parse("set {target::*} to {source::*}", "failed");
        Variable<String> first = Variable.newInstance("target::1", new Class[]{String.class});
        Variable<String> second = Variable.newInstance("target::2", new Class[]{String.class});

        assertNotNull(setTen);
        assertNotNull(setTwo);
        assertNotNull(copyList);
        assertNotNull(first);
        assertNotNull(second);

        TriggerItem.walk(setTen, SkriptEvent.EMPTY);
        TriggerItem.walk(setTwo, SkriptEvent.EMPTY);
        TriggerItem.walk(copyList, SkriptEvent.EMPTY);

        assertEquals("gold_block", first.getSingle(SkriptEvent.EMPTY));
        assertEquals("emerald_block", second.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parsedListToListSetSkipsNestedDescendantEntriesFromShallowSourceRead() {
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );

        Statement setNested = Statement.parse("set {source::group::1} to \"gold_block\"", "failed");
        Statement setPlain = Statement.parse("set {source::plain} to \"emerald_block\"", "failed");
        Statement copyList = Statement.parse("set {target::*} to {source::*}", "failed");
        Variable<String> first = Variable.newInstance("target::1", new Class[]{String.class});
        Variable<String> second = Variable.newInstance("target::2", new Class[]{String.class});

        assertNotNull(setNested);
        assertNotNull(setPlain);
        assertNotNull(copyList);
        assertNotNull(first);
        assertNotNull(second);

        TriggerItem.walk(setNested, SkriptEvent.EMPTY);
        TriggerItem.walk(setPlain, SkriptEvent.EMPTY);
        TriggerItem.walk(copyList, SkriptEvent.EMPTY);

        assertEquals("emerald_block", first.getSingle(SkriptEvent.EMPTY));
        assertNull(second.getSingle(SkriptEvent.EMPTY));
    }
}
