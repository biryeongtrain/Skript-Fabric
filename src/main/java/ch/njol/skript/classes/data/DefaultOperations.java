package ch.njol.skript.classes.data;

import ch.njol.skript.util.Timespan;
import java.math.BigInteger;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;

/**
 * Pure-Java subset of upstream default arithmetic registrations.
 */
public final class DefaultOperations {

    private DefaultOperations() {
    }

    public static void register() {
        registerNumberOperations();
        registerTimespanOperations();
        registerStringOperations();
    }

    private static void registerNumberOperations() {
        registerNumberOperation(Operator.ADDITION, DefaultOperations::addNumbers);
        registerNumberOperation(Operator.SUBTRACTION, DefaultOperations::subtractNumbers);
        registerNumberOperation(Operator.MULTIPLICATION, DefaultOperations::multiplyNumbers);
        registerNumberOperation(Operator.DIVISION, Number.class,
                (left, right) -> left.doubleValue() / right.doubleValue());
        registerNumberOperation(Operator.EXPONENTIATION, Number.class,
                (left, right) -> Math.pow(left.doubleValue(), right.doubleValue()));
        if (!Arithmetics.exactDifferenceExists(Number.class)) {
            Arithmetics.registerDifference(Number.class, (left, right) -> {
                if (isInteger(left, right)) {
                    BigInteger difference = BigInteger.valueOf(left.longValue())
                            .subtract(BigInteger.valueOf(right.longValue()))
                            .abs();
                    if (difference.bitLength() < Long.SIZE) {
                        return difference.longValue();
                    }
                }
                return Math.abs(left.doubleValue() - right.doubleValue());
            });
        }
        Arithmetics.registerDefaultValue(Number.class, () -> 0L);
    }

    private static void registerTimespanOperations() {
        if (!Arithmetics.exactOperationExists(Operator.ADDITION, Timespan.class, Timespan.class)) {
            Arithmetics.registerOperation(Operator.ADDITION, Timespan.class, Timespan::add);
        }
        if (!Arithmetics.exactOperationExists(Operator.SUBTRACTION, Timespan.class, Timespan.class)) {
            Arithmetics.registerOperation(Operator.SUBTRACTION, Timespan.class, Timespan::subtract);
        }
        if (!Arithmetics.exactOperationExists(Operator.DIVISION, Timespan.class, Timespan.class)) {
            Arithmetics.registerOperation(
                    Operator.DIVISION,
                    Timespan.class,
                    Timespan.class,
                    Number.class,
                    Timespan::divide
            );
        }
        if (!Arithmetics.exactOperationExists(Operator.MULTIPLICATION, Timespan.class, Number.class)) {
            Arithmetics.registerOperation(
                    Operator.MULTIPLICATION,
                    Timespan.class,
                    Number.class,
                    scaleTimespan(),
                    scaleTimespanCommutative()
            );
        }
        if (!Arithmetics.exactOperationExists(Operator.DIVISION, Timespan.class, Number.class)) {
            Arithmetics.registerOperation(Operator.DIVISION, Timespan.class, Number.class, (left, right) -> {
                double scalar = right.doubleValue();
                if (scalar < 0 || Double.isNaN(scalar)) {
                    return null;
                }
                return left.divide(scalar);
            });
        }
        if (!Arithmetics.exactDifferenceExists(Timespan.class)) {
            Arithmetics.registerDifference(Timespan.class, Timespan::difference);
        }
        Arithmetics.registerDefaultValue(Timespan.class, Timespan::new);
    }

    private static void registerStringOperations() {
        if (!Arithmetics.exactOperationExists(Operator.ADDITION, String.class, String.class)) {
            Arithmetics.registerOperation(Operator.ADDITION, String.class, String.class, String::concat);
        }
    }

    private static void registerNumberOperation(
            Operator operator,
            org.skriptlang.skript.lang.arithmetic.Operation<Number, Number, Number> operation
    ) {
        registerNumberOperation(operator, Number.class, operation);
    }

    private static void registerNumberOperation(
            Operator operator,
            Class<?> returnType,
            org.skriptlang.skript.lang.arithmetic.Operation<Number, Number, ?> operation
    ) {
        if (!Arithmetics.exactOperationExists(operator, Number.class, Number.class)) {
            @SuppressWarnings("unchecked")
            Class<Number> castReturnType = (Class<Number>) returnType;
            @SuppressWarnings("unchecked")
            org.skriptlang.skript.lang.arithmetic.Operation<Number, Number, Number> castOperation =
                    (org.skriptlang.skript.lang.arithmetic.Operation<Number, Number, Number>) operation;
            Arithmetics.registerOperation(operator, Number.class, Number.class, castReturnType, castOperation);
        }
    }

    private static org.skriptlang.skript.lang.arithmetic.Operation<Timespan, Number, Timespan> scaleTimespan() {
        return (left, right) -> {
            double scalar = right.doubleValue();
            if (scalar < 0 || Double.isNaN(scalar)) {
                return null;
            }
            return left.multiply(scalar);
        };
    }

    private static org.skriptlang.skript.lang.arithmetic.Operation<Number, Timespan, Timespan> scaleTimespanCommutative() {
        return (left, right) -> {
            double scalar = left.doubleValue();
            if (scalar < 0 || Double.isNaN(scalar)) {
                return null;
            }
            return right.multiply(scalar);
        };
    }

    private static Number addNumbers(Number left, Number right) {
        if (isInteger(left, right)) {
            BigInteger result = BigInteger.valueOf(left.longValue()).add(BigInteger.valueOf(right.longValue()));
            if (result.bitLength() < Long.SIZE) {
                return result.longValue();
            }
        }
        return left.doubleValue() + right.doubleValue();
    }

    private static Number subtractNumbers(Number left, Number right) {
        if (isInteger(left, right)) {
            BigInteger result = BigInteger.valueOf(left.longValue()).subtract(BigInteger.valueOf(right.longValue()));
            if (result.bitLength() < Long.SIZE) {
                return result.longValue();
            }
        }
        return left.doubleValue() - right.doubleValue();
    }

    private static Number multiplyNumbers(Number left, Number right) {
        if (isInteger(left, right)) {
            BigInteger result = BigInteger.valueOf(left.longValue()).multiply(BigInteger.valueOf(right.longValue()));
            if (result.bitLength() < Long.SIZE) {
                return result.longValue();
            }
        }
        return left.doubleValue() * right.doubleValue();
    }

    private static boolean isInteger(Number left, Number right) {
        return isIntegral(left) && isIntegral(right);
    }

    private static boolean isIntegral(Number number) {
        return number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long;
    }
}
