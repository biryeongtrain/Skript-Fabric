package ch.njol.skript.classes.data;

import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.KeyedValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import java.text.DecimalFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Pure-Java subset of upstream default function registrations.
 */
public final class DefaultFunctions {

    private static final DecimalFormat DEFAULT_INTEGER_FORMAT = new DecimalFormat("###,###");
    private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("###,###.##");

    private DefaultFunctions() {
    }

    public static void register() {
        if (Functions.getFunction("date") != null && Functions.getFunction("round") != null) {
            return;
        }
        ClassInfo<Object> objectInfo = classInfo(Object.class);
        ClassInfo<Number> numberInfo = classInfo(Number.class);
        ClassInfo<Long> longInfo = classInfo(Long.class);
        ClassInfo<Boolean> booleanInfo = classInfo(Boolean.class);
        ClassInfo<String> stringInfo = classInfo(String.class);
        ClassInfo<Date> dateInfo = classInfo(Date.class);
        Parameter<?>[] numberParam = {new Parameter<>("n", numberInfo, true, null)};
        Parameter<?>[] numbersParam = {new Parameter<>("ns", numberInfo, false, null)};

        Functions.register(new SimpleJavaFunction<Long>("floor", numberParam, longInfo, true) {
            @Override
            public Long[] executeSimple(Object[][] params) {
                Number value = (Number) params[0][0];
                if (value instanceof Long whole) {
                    return new Long[]{whole};
                }
                return new Long[]{(long) Math.floor(value.doubleValue())};
            }
        });

        Functions.register(new SimpleJavaFunction<Number>(
                        "round",
                        new Parameter[]{
                        new Parameter<>("n", numberInfo, true, null),
                        new Parameter<>("d", numberInfo, true, new SimpleLiteral<>(0, true))
                },
                numberInfo,
                true
        ) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                if (params[0][0] instanceof Long whole) {
                    return new Number[]{whole};
                }
                double value = ((Number) params[0][0]).doubleValue();
                if (!Double.isFinite(value)) {
                    return new Number[]{value};
                }
                double placementDouble = ((Number) params[1][0]).doubleValue();
                if (!Double.isFinite(placementDouble)
                        || placementDouble >= Integer.MAX_VALUE
                        || placementDouble <= Integer.MIN_VALUE) {
                    return new Number[]{Double.NaN};
                }
                int placement = (int) placementDouble;
                if (placement == 0) {
                    return new Number[]{Math.round(value)};
                }
                if (placement >= 0) {
                    BigDecimal decimal = new BigDecimal(Double.toString(value));
                    decimal = decimal.setScale(placement, RoundingMode.HALF_UP);
                    return new Number[]{decimal.doubleValue()};
                }
                long rounded = Math.round(value);
                return new Number[]{
                        (int) Math.round(rounded * Math.pow(10.0, placement)) / Math.pow(10.0, placement)
                };
            }
        });

        registerUnaryNumberFunction("ceil", numberParam, value -> value instanceof Long whole ? whole : (long) Math.ceil(value.doubleValue()), numberInfo);
        registerUnaryNumberFunction("ceiling", numberParam, value -> value instanceof Long whole ? whole : (long) Math.ceil(value.doubleValue()), numberInfo);
        registerUnaryNumberFunction("abs", numberParam, value -> isIntegral(value) ? Math.abs(value.longValue()) : Math.abs(value.doubleValue()), numberInfo);
        registerUnaryNumberFunction("exp", numberParam, value -> Math.exp(value.doubleValue()), numberInfo);
        registerUnaryNumberFunction("ln", numberParam, value -> Math.log(value.doubleValue()), numberInfo);
        registerUnaryNumberFunction("sqrt", numberParam, value -> Math.sqrt(value.doubleValue()), numberInfo);
        registerUnaryNumberFunction("sin", numberParam, value -> Math.sin(Math.toRadians(value.doubleValue())), numberInfo);
        registerUnaryNumberFunction("cos", numberParam, value -> Math.cos(Math.toRadians(value.doubleValue())), numberInfo);
        registerUnaryNumberFunction("tan", numberParam, value -> Math.tan(Math.toRadians(value.doubleValue())), numberInfo);
        registerUnaryNumberFunction("asin", numberParam, value -> Math.toDegrees(Math.asin(value.doubleValue())), numberInfo);
        registerUnaryNumberFunction("acos", numberParam, value -> Math.toDegrees(Math.acos(value.doubleValue())), numberInfo);
        registerUnaryNumberFunction("atan", numberParam, value -> Math.toDegrees(Math.atan(value.doubleValue())), numberInfo);
        registerUnaryBooleanFunction("isNaN", numberParam, value -> Double.isNaN(value.doubleValue()), booleanInfo);

        Functions.register(new SimpleJavaFunction<Number>(
                "mod",
                new Parameter[]{
                        new Parameter<>("d", numberInfo, true, null),
                        new Parameter<>("m", numberInfo, true, null)
                },
                numberInfo,
                true
        ) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                Number dividend = (Number) params[0][0];
                double modulus = ((Number) params[1][0]).doubleValue();
                if (modulus == 0) {
                    return new Number[]{Double.NaN};
                }
                double result = ((dividend.doubleValue() % modulus) + modulus) % modulus;
                return new Number[]{result};
            }
        });

        Functions.register(new SimpleJavaFunction<Number>(
                "log",
                new Parameter[]{
                        new Parameter<>("n", numberInfo, true, null),
                        new Parameter<>("base", numberInfo, true, new SimpleLiteral<>(10, true))
                },
                numberInfo,
                true
        ) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                double value = ((Number) params[0][0]).doubleValue();
                double base = ((Number) params[1][0]).doubleValue();
                return new Number[]{Math.log10(value) / Math.log10(base)};
            }
        });

        Functions.register(new SimpleJavaFunction<Number>(
                "atan2",
                new Parameter[]{
                        new Parameter<>("x", numberInfo, true, null),
                        new Parameter<>("y", numberInfo, true, null)
                },
                numberInfo,
                true
        ) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                double x = ((Number) params[0][0]).doubleValue();
                double y = ((Number) params[1][0]).doubleValue();
                return new Number[]{Math.toDegrees(Math.atan2(y, x))};
            }
        });

        Functions.register(new SimpleJavaFunction<Number>("sum", numbersParam, numberInfo, true) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                Object[] values = values(params[0]);
                double sum = ((Number) values[0]).doubleValue();
                for (int index = 1; index < values.length; index++) {
                    sum += ((Number) values[index]).doubleValue();
                }
                return new Number[]{sum};
            }
        });

        Functions.register(new SimpleJavaFunction<Number>("product", numbersParam, numberInfo, true) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                Object[] values = values(params[0]);
                double product = ((Number) values[0]).doubleValue();
                for (int index = 1; index < values.length; index++) {
                    product *= ((Number) values[index]).doubleValue();
                }
                return new Number[]{product};
            }
        });

        Functions.register(new SimpleJavaFunction<Number>("max", numbersParam, numberInfo, true) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                Object[] values = values(params[0]);
                double max = ((Number) values[0]).doubleValue();
                for (int index = 1; index < values.length; index++) {
                    double candidate = ((Number) values[index]).doubleValue();
                    if (candidate > max || Double.isNaN(max)) {
                        max = candidate;
                    }
                }
                return new Number[]{max};
            }
        });

        Functions.register(new SimpleJavaFunction<Number>("min", numbersParam, numberInfo, true) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                Object[] values = values(params[0]);
                double min = ((Number) values[0]).doubleValue();
                for (int index = 1; index < values.length; index++) {
                    double candidate = ((Number) values[index]).doubleValue();
                    if (candidate < min || Double.isNaN(min)) {
                        min = candidate;
                    }
                }
                return new Number[]{min};
            }
        });

        Functions.register(new SimpleJavaFunction<String>(
                "toBase",
                new Parameter[]{
                        new Parameter<>("n", longInfo, false, null),
                        new Parameter<>("base", longInfo, true, null)
                },
                stringInfo,
                false
        ) {
            @Override
            public String[] executeSimple(Object[][] params) {
                Object[] values = values(params[0]);
                int base = ((Long) params[1][0]).intValue();
                if (base < 2 || base > 36) {
                    return null;
                }
                String[] result = new String[values.length];
                for (int index = 0; index < values.length; index++) {
                    result[index] = Long.toString((Long) values[index], base);
                }
                return result;
            }
        });

        Functions.register(new SimpleJavaFunction<Long>(
                "fromBase",
                new Parameter[]{
                        new Parameter<>("string value", stringInfo, false, null),
                        new Parameter<>("base", longInfo, true, null)
                },
                longInfo,
                false
        ) {
            @Override
            public Long[] executeSimple(Object[][] params) {
                Object[] values = values(params[0]);
                int base = ((Long) params[1][0]).intValue();
                if (base < 2 || base > 36) {
                    return null;
                }
                Long[] result = new Long[values.length];
                try {
                    for (int index = 0; index < values.length; index++) {
                        result[index] = Long.parseLong((String) values[index], base);
                    }
                } catch (NumberFormatException ex) {
                    return null;
                }
                return result;
            }
        });

        Functions.register(new SimpleJavaFunction<Date>(
                "date",
                new Parameter[]{
                        new Parameter<>("year", numberInfo, true, null),
                        new Parameter<>("month", numberInfo, true, null),
                        new Parameter<>("day", numberInfo, true, null),
                        new Parameter<>("hour", numberInfo, true, new SimpleLiteral<>(0, true)),
                        new Parameter<>("minute", numberInfo, true, new SimpleLiteral<>(0, true)),
                        new Parameter<>("second", numberInfo, true, new SimpleLiteral<>(0, true)),
                        new Parameter<>("millisecond", numberInfo, true, new SimpleLiteral<>(0, true)),
                        new Parameter<>("zone_offset", numberInfo, true, new SimpleLiteral<>(Double.NaN, true)),
                        new Parameter<>("dst_offset", numberInfo, true, new SimpleLiteral<>(Double.NaN, true))
                },
                dateInfo,
                true
        ) {
            @Override
            public Date[] executeSimple(Object[][] params) {
                int year = ((Number) params[0][0]).intValue();
                int month = ((Number) params[1][0]).intValue();
                int day = ((Number) params[2][0]).intValue();
                int hour = ((Number) params[3][0]).intValue();
                int minute = ((Number) params[4][0]).intValue();
                int second = ((Number) params[5][0]).intValue();
                int millisecond = ((Number) params[6][0]).intValue();
                double zoneOffsetMinutes = ((Number) params[7][0]).doubleValue();
                double dstOffsetMinutes = ((Number) params[8][0]).doubleValue();

                ZoneOffset offset = zoneOffsetMinutes == zoneOffsetMinutes
                        ? ZoneOffset.ofTotalSeconds((int) Math.round((zoneOffsetMinutes + nanSafe(dstOffsetMinutes)) * 60))
                        : ZoneOffset.UTC;
                ZonedDateTime zonedDateTime = ZonedDateTime.of(
                        year,
                        month,
                        day,
                        hour,
                        minute,
                        second,
                        millisecond * 1_000_000,
                        offset
                );
                return new Date[]{new Date(zonedDateTime.toInstant().toEpochMilli())};
            }

            private double nanSafe(double value) {
                return value == value ? value : 0.0D;
            }
        });

        Functions.register(new SimpleJavaFunction<Long>("calcExperience",
                new Parameter[]{new Parameter<>("level", longInfo, true, null)},
                longInfo,
                true
        ) {
            @Override
            public Long[] executeSimple(Object[][] params) {
                long level = (Long) params[0][0];
                long experience;
                if (level <= 0) {
                    experience = 0;
                } else if (level <= 15) {
                    experience = level * level + 6 * level;
                } else if (level <= 30) {
                    experience = (int) (2.5 * level * level - 40.5 * level + 360);
                } else {
                    experience = (int) (4.5 * level * level - 162.5 * level + 2220);
                }
                return new Long[]{experience};
            }
        });

        Functions.register(new SimpleJavaFunction<String>(
                "concat",
                new Parameter[]{new Parameter<>("texts", objectInfo, false, null)},
                stringInfo,
                true
        ) {
            @Override
            public String[] executeSimple(Object[][] params) {
                StringBuilder builder = new StringBuilder();
                for (Object object : values(params[0])) {
                    builder.append(Classes.toString(object, ch.njol.skript.util.StringMode.MESSAGE));
                }
                return new String[]{builder.toString()};
            }
        });

        Functions.register(new SimpleJavaFunction<String>(
                "formatNumber",
                new Parameter[]{
                        new Parameter<>("number", numberInfo, true, null),
                        new Parameter<>("format", stringInfo, true, new SimpleLiteral<>("", true))
                },
                stringInfo,
                true
        ) {
            @Override
            public String[] executeSimple(Object[][] params) {
                Number number = (Number) params[0][0];
                String pattern = (String) params[1][0];
                if (pattern.isEmpty()) {
                    DecimalFormat format = (number instanceof Float || number instanceof Double)
                            ? DEFAULT_DECIMAL_FORMAT
                            : DEFAULT_INTEGER_FORMAT;
                    return new String[]{format.format(number)};
                }
                try {
                    return new String[]{new DecimalFormat(pattern).format(number)};
                } catch (IllegalArgumentException ex) {
                    return null;
                }
            }
        });

        Functions.register(new SimpleJavaFunction<Number>(
                "clamp",
                new Parameter[]{
                        new Parameter<>("value", numberInfo, true, null),
                        new Parameter<>("min", numberInfo, true, null),
                        new Parameter<>("max", numberInfo, true, null)
                },
                numberInfo,
                true
        ) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                double value = ((Number) params[0][0]).doubleValue();
                double min = ((Number) params[1][0]).doubleValue();
                double max = ((Number) params[2][0]).doubleValue();
                return new Number[]{Math.max(min, Math.min(max, value))};
            }
        });

        ClassInfo<Vec3> vectorInfo = classInfo(Vec3.class);
        Functions.register(new SimpleJavaFunction<Vec3>(
                "vector",
                new Parameter[]{
                        new Parameter<>("x", numberInfo, true, null),
                        new Parameter<>("y", numberInfo, true, null),
                        new Parameter<>("z", numberInfo, true, null)
                },
                vectorInfo,
                true
        ) {
            @Override
            public Vec3[] executeSimple(Object[][] params) {
                double x = ((Number) params[0][0]).doubleValue();
                double y = ((Number) params[1][0]).doubleValue();
                double z = ((Number) params[2][0]).doubleValue();
                return new Vec3[]{new Vec3(x, y, z)};
            }
        });

        ClassInfo<FabricLocation> locationInfo = classInfo(FabricLocation.class);
        Functions.register(new SimpleJavaFunction<FabricLocation>(
                "location",
                new Parameter[]{
                        new Parameter<>("x", numberInfo, true, null),
                        new Parameter<>("y", numberInfo, true, null),
                        new Parameter<>("z", numberInfo, true, null),
                        new Parameter<>("yaw", numberInfo, true, new SimpleLiteral<>(0, true)),
                        new Parameter<>("pitch", numberInfo, true, new SimpleLiteral<>(0, true))
                },
                locationInfo,
                true
        ) {
            @Override
            public FabricLocation[] executeSimple(Object[][] params) {
                double x = ((Number) params[0][0]).doubleValue();
                double y = ((Number) params[1][0]).doubleValue();
                double z = ((Number) params[2][0]).doubleValue();
                return new FabricLocation[]{new FabricLocation(null, new Vec3(x, y, z))};
            }
        });
    }

    private static void registerUnaryNumberFunction(
            String name,
            Parameter<?>[] parameters,
            java.util.function.Function<Number, Number> function,
            ClassInfo<Number> returnType
    ) {
        Functions.register(new SimpleJavaFunction<Number>(name, parameters, returnType, true) {
            @Override
            public Number[] executeSimple(Object[][] params) {
                return new Number[]{function.apply((Number) params[0][0])};
            }
        });
    }

    private static void registerUnaryBooleanFunction(
            String name,
            Parameter<?>[] parameters,
            java.util.function.Function<Number, Boolean> function,
            ch.njol.skript.classes.ClassInfo<Boolean> returnType
    ) {
        Functions.register(new SimpleJavaFunction<Boolean>(name, parameters, returnType, true) {
            @Override
            public Boolean[] executeSimple(Object[][] params) {
                return new Boolean[]{function.apply((Number) params[0][0])};
            }
        });
    }

    private static boolean isIntegral(Number number) {
        return number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long;
    }

    private static Object[] values(Object[] rawValues) {
        if (rawValues instanceof KeyedValue<?>[] keyedValues) {
            Object[] unwrapped = new Object[keyedValues.length];
            for (int index = 0; index < keyedValues.length; index++) {
                unwrapped[index] = keyedValues[index].value();
            }
            return unwrapped;
        }
        return rawValues;
    }

    private static <T> ClassInfo<T> classInfo(Class<T> type) {
        ClassInfo<T> info = Classes.getExactClassInfo(type);
        if (info == null) {
            throw new IllegalStateException("Missing required class info for " + type.getName());
        }
        return info;
    }
}
