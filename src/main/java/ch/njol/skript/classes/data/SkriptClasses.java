package ch.njol.skript.classes.data;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import java.util.Iterator;
import org.jetbrains.annotations.Nullable;

/**
 * Serializer-free subset of upstream Skript class registrations.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class SkriptClasses {

    private SkriptClasses() {
    }

    public static void register() {
        registerClassInfo();
        registerTime();
        registerTimeperiod();
        registerDate();
        registerFunctionReference();
    }

    private static void registerClassInfo() {
        if (Classes.getExactClassInfo(ClassInfo.class) != null) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(ClassInfo.class, "classinfo")
                .user("types?")
                .name("Type")
                .supplier(() -> (Iterator) Classes.getClassInfos().iterator())
                .parser(new Parser<ClassInfo>() {
                    @Override
                    public @Nullable ClassInfo parse(String input, ParseContext context) {
                        return Classes.getClassInfoFromUserInput(input);
                    }

                    @Override
                    public String toString(ClassInfo object, int flags) {
                        return object.getCodeName();
                    }

                    @Override
                    public String toVariableNameString(ClassInfo object) {
                        return object.getCodeName();
                    }

                    @Override
                    public String getDebugMessage(ClassInfo object) {
                        return object.getCodeName();
                    }
                }));
    }

    private static void registerTime() {
        if (Classes.getExactClassInfo(Time.class) != null) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(Time.class, "time")
                .user("times?")
                .name("Time")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable Time parse(String input, ParseContext context) {
                        return Time.parse(input);
                    }

                    @Override
                    public String toString(Time object, int flags) {
                        return object.toString();
                    }

                    @Override
                    public String toVariableNameString(Time object) {
                        return "time:" + object.getTicks();
                    }
                }));
    }

    private static void registerTimeperiod() {
        if (Classes.getExactClassInfo(Timeperiod.class) != null) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(Timeperiod.class, "timeperiod")
                .user("time ?periods?", "durations?")
                .name("Timeperiod")
                .before("timespan")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable Timeperiod parse(String input, ParseContext context) {
                        if (input.equalsIgnoreCase("day")) {
                            return new Timeperiod(0, 11999);
                        }
                        if (input.equalsIgnoreCase("dusk")) {
                            return new Timeperiod(12000, 13799);
                        }
                        if (input.equalsIgnoreCase("night")) {
                            return new Timeperiod(13800, 22199);
                        }
                        if (input.equalsIgnoreCase("dawn")) {
                            return new Timeperiod(22200, 23999);
                        }
                        int split = input.indexOf('-');
                        if (split == -1) {
                            Time time = Time.parse(input);
                            return time == null ? null : new Timeperiod(time.getTicks());
                        }
                        Time start = Time.parse(input.substring(0, split).trim());
                        Time end = Time.parse(input.substring(split + 1).trim());
                        if (start == null || end == null) {
                            return null;
                        }
                        return new Timeperiod(start.getTicks(), end.getTicks());
                    }

                    @Override
                    public String toString(Timeperiod object, int flags) {
                        return object.toString();
                    }

                    @Override
                    public String toVariableNameString(Timeperiod object) {
                        return "timeperiod:" + object.start + "-" + object.end;
                    }
                }));
    }

    private static void registerDate() {
        if (Classes.getExactClassInfo(Date.class) != null) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(Date.class, "date")
                .user("dates?")
                .name("Date"));
    }

    private static void registerFunctionReference() {
        if (Classes.getExactClassInfo(DynamicFunctionReference.class) != null) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(DynamicFunctionReference.class, "function")
                .user("functions?")
                .name("Function")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable DynamicFunctionReference parse(String input, ParseContext context) {
                        return DynamicFunctionReference.parseFunction(input);
                    }

                    @Override
                    public String toString(DynamicFunctionReference object, int flags) {
                        return object.toString();
                    }

                    @Override
                    public String toVariableNameString(DynamicFunctionReference object) {
                        return object.toString();
                    }
                }));
    }
}
