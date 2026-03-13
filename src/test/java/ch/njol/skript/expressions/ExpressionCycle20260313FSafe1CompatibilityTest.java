package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.lang.util.common.AnyValued;
import ch.njol.skript.registrations.Classes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExpressionCycle20260313FSafe1CompatibilityTest {

    @BeforeAll
    static void registerSyntax() {
        JavaClasses.register();
        SkriptClasses.register();
    }

    @Test
    void parseExpressionParsesTypesAndReportsErrors() {
        ExprParse parseInteger = new ExprParse();
        assertTrue(parseInteger.init(new Expression[]{
                new SimpleLiteral<>("12", false),
                new SimpleLiteral<>(Classes.getExactClassInfo(Integer.class), false)
        }, 0, ch.njol.util.Kleenean.FALSE, parseResult("\"12\" parsed as integer")));
        assertEquals(12, parseInteger.getSingle(SkriptEvent.EMPTY));
        assertNull(ExprParse.lastError);

        ExprParse failInteger = new ExprParse();
        assertTrue(failInteger.init(new Expression[]{
                new SimpleLiteral<>("nope", false),
                new SimpleLiteral<>(Classes.getExactClassInfo(Integer.class), false)
        }, 0, ch.njol.util.Kleenean.FALSE, parseResult("\"nope\" parsed as integer")));
        assertNull(failInteger.getSingle(SkriptEvent.EMPTY));

        ExprParseError parseError = new ExprParseError();
        assertTrue(parseError.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("parse error")));
        assertNotNull(parseError.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void valueExpressionsReadChangeAndPreserveKeys() {
        ClassInfo<String> stringInfo = Classes.getExactClassInfo(String.class);
        assertNotNull(stringInfo);

        ExprValue value = new ExprValue();
        MutableAnyValued valued = new MutableAnyValued("alpha");
        assertTrue(value.init(new Expression[]{
                new SimpleLiteral<>(stringInfo, false),
                new SimpleLiteral<>(valued, false)
        }, 0, ch.njol.util.Kleenean.FALSE, parseResult("string value")));
        assertEquals("alpha", value.getSingle(SkriptEvent.EMPTY));
        value.change(SkriptEvent.EMPTY, new Object[]{"beta"}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertEquals("beta", valued.value());

    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        return parseResult(expr, null);
    }

    private static SkriptParser.ParseResult parseResult(String expr, @Nullable String regexGroup) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        if (regexGroup != null) {
            Matcher matcher = Pattern.compile(".*", Pattern.DOTALL).matcher(regexGroup);
            matcher.find();
            result.regexes = List.of(matcher);
        }
        return result;
    }

    private static final class MutableAnyValued implements AnyValued<String> {
        private String value;

        private MutableAnyValued(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public boolean supportsValueChange() {
            return true;
        }

        @Override
        public void changeValue(String value) {
            this.value = value;
        }

        @Override
        public Class<String> valueType() {
            return String.class;
        }
    }
}
