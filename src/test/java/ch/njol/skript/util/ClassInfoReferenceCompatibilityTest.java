package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.junit.jupiter.api.Test;

class ClassInfoReferenceCompatibilityTest {

    @Test
    void wrapPreservesPluralityFromSourceLiteral() {
        ClassInfo<String> classInfo = new ClassInfo<>(String.class, "item");
        Expression<ClassInfo<?>> expression = new SimpleLiteral<>(classInfo, false, new UnparsedLiteral("items"));

        Expression<ClassInfoReference> wrapped = ClassInfoReference.wrap(expression);
        ClassInfoReference reference = wrapped.getSingle(null);

        assertEquals(Kleenean.TRUE, reference.isPlural());
        assertSame(classInfo, reference.getClassInfo());
    }

    @Test
    void wrapKeepsExpressionListsAsExpressionLists() {
        ClassInfo<String> itemInfo = new ClassInfo<>(String.class, "item");
        ClassInfo<Number> numberInfo = new ClassInfo<>(Number.class, "number");
        Expression<ClassInfo<?>> first = new SimpleLiteral<>(itemInfo, false, new UnparsedLiteral("items"));
        Expression<ClassInfo<?>> second = new SimpleLiteral<>(numberInfo, false, new UnparsedLiteral("numbers"));
        Expression<ClassInfo<?>> list = new ExpressionList<>(new Expression[]{first, second}, ClassInfo.class, true);

        Expression<ClassInfoReference> wrapped = ClassInfoReference.wrap(list);

        assertInstanceOf(ExpressionList.class, wrapped);
        assertTrue(wrapped.getAnd());
        assertEquals(2, wrapped.getAll(null).length);
    }
}
