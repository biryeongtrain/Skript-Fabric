package ch.njol.skript.util;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ClassInfoReference {

    @Nullable
    private static UnparsedLiteral getSourceUnparsedLiteral(Expression<?> expression) {
        while (!(expression instanceof UnparsedLiteral)) {
            Expression<?> nextExpression = expression.getSource();
            if (nextExpression == expression) {
                return null;
            }
            expression = nextExpression;
        }
        return (UnparsedLiteral) expression;
    }

    private static Kleenean determineIfPlural(Expression<ClassInfo<?>> classInfoExpression) {
        UnparsedLiteral sourceUnparsedLiteral = getSourceUnparsedLiteral(classInfoExpression);
        if (sourceUnparsedLiteral == null) {
            return Kleenean.UNKNOWN;
        }
        String originalExpression = sourceUnparsedLiteral.getData();
        boolean isPlural = originalExpression.endsWith("s");
        return Kleenean.get(isPlural);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static Expression<ClassInfoReference> wrap(@NotNull Expression<ClassInfo<?>> classInfoExpression) {
        if (classInfoExpression instanceof ExpressionList<?> classInfoExpressionList) {
            Expression<ClassInfoReference>[] wrappedExpressions = Arrays.stream(classInfoExpressionList.getExpressions())
                    .map(expression -> wrap((Expression<ClassInfo<?>>) expression))
                    .toArray(Expression[]::new);
            return new ExpressionList<>(wrappedExpressions, ClassInfoReference.class, classInfoExpression.getAnd());
        }

        Kleenean isPlural = determineIfPlural(classInfoExpression);
        if (classInfoExpression instanceof Literal<ClassInfo<?>> classInfoLiteral) {
            ClassInfo<?> classInfo = classInfoLiteral.getSingle(null);
            return new SimpleLiteral<>(new ClassInfoReference(classInfo, isPlural), classInfoLiteral.isDefault());
        }

        return new SimpleExpression<>() {
            @Override
            protected ClassInfoReference @Nullable [] get(SkriptEvent event) {
                if (classInfoExpression.isSingle()) {
                    ClassInfo<?> classInfo = classInfoExpression.getSingle(event);
                    if (classInfo == null) {
                        return new ClassInfoReference[0];
                    }
                    return new ClassInfoReference[]{new ClassInfoReference(classInfo, isPlural)};
                }
                return classInfoExpression.stream(event)
                        .map(classInfo -> new ClassInfoReference(classInfo, isPlural))
                        .toArray(ClassInfoReference[]::new);
            }

            @Override
            public boolean isSingle() {
                return classInfoExpression.isSingle();
            }

            @Override
            public Class<? extends ClassInfoReference> getReturnType() {
                return ClassInfoReference.class;
            }

            @Override
            public String toString(@Nullable SkriptEvent event, boolean debug) {
                if (debug) {
                    return classInfoExpression.toString(event, true) + "(wrapped by " + getClass().getSimpleName() + ")";
                }
                return classInfoExpression.toString(event, false);
            }
        };
    }

    private Kleenean plural;
    private ClassInfo<?> classInfo;

    public ClassInfoReference(ClassInfo<?> classInfo) {
        this(classInfo, Kleenean.UNKNOWN);
    }

    public ClassInfoReference(ClassInfo<?> classInfo, Kleenean plural) {
        this.classInfo = classInfo;
        this.plural = plural;
    }

    public Kleenean isPlural() {
        return plural;
    }

    public void setPlural(Kleenean plural) {
        this.plural = plural;
    }

    public ClassInfo<?> getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassInfo<?> classInfo) {
        this.classInfo = classInfo;
    }
}
