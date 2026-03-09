package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.Kleenean;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.event.Event;

public final class EventValues {

    public static final int TIME_PAST = -1;
    public static final int TIME_NOW = 0;
    public static final int TIME_FUTURE = 1;

    private static final List<EventValueInfo<?, ?>> DEFAULT_EVENT_VALUES = new ArrayList<>(30);
    private static final List<EventValueInfo<?, ?>> FUTURE_EVENT_VALUES = new ArrayList<>();
    private static final List<EventValueInfo<?, ?>> PAST_EVENT_VALUES = new ArrayList<>();

    private EventValues() {
    }

    public static List<EventValueInfo<?, ?>> getEventValuesListForTime(int time) {
        return ImmutableList.copyOf(getEventValuesList(time));
    }

    private static List<EventValueInfo<?, ?>> getEventValuesList(int time) {
        if (time == TIME_PAST) {
            return PAST_EVENT_VALUES;
        }
        if (time == TIME_NOW) {
            return DEFAULT_EVENT_VALUES;
        }
        if (time == TIME_FUTURE) {
            return FUTURE_EVENT_VALUES;
        }
        throw new IllegalArgumentException("time must be -1, 0, or 1");
    }

    public static <T, E extends Event> void registerEventValue(
            Class<E> eventClass,
            Class<T> valueClass,
            Converter<E, T> converter
    ) {
        registerEventValue(eventClass, valueClass, converter, TIME_NOW);
    }

    public static <T, E extends Event> void registerEventValue(
            Class<E> eventClass,
            Class<T> valueClass,
            Converter<E, T> converter,
            int time
    ) {
        registerEventValue(eventClass, valueClass, converter, time, null, (Class<? extends E>[]) null);
    }

    @SafeVarargs
    public static <T, E extends Event> void registerEventValue(
            Class<E> eventClass,
            Class<T> valueClass,
            Converter<E, T> converter,
            int time,
            @Nullable String excludeErrorMessage,
            @Nullable Class<? extends E>... excludes
    ) {
        Skript.checkAcceptRegistrations();
        List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
        EventValueInfo<E, T> element = new EventValueInfo<>(eventClass, valueClass, converter, excludeErrorMessage, excludes, time);

        for (int i = 0; i < eventValues.size(); i++) {
            EventValueInfo<?, ?> info = eventValues.get(i);
            if (info.eventClass.equals(eventClass) && info.valueClass.equals(valueClass)) {
                return;
            }
            if (!info.eventClass.equals(eventClass)
                    ? info.eventClass.isAssignableFrom(eventClass)
                    : info.valueClass.isAssignableFrom(valueClass)) {
                eventValues.add(i, element);
                return;
            }
        }
        eventValues.add(element);
    }

    public static <T, E extends Event> @Nullable T getEventValue(E event, Class<T> valueClass, int time) {
        @SuppressWarnings("unchecked")
        Converter<? super E, ? extends T> converter =
                getEventValueConverter((Class<E>) event.getClass(), valueClass, time);
        if (converter == null) {
            return null;
        }
        return converter.convert(event);
    }

    public static <E extends Event, T> @Nullable Converter<? super E, ? extends T> getExactEventValueConverter(
            Class<E> eventClass,
            Class<T> valueClass,
            int time
    ) {
        List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
        for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
            if (!valueClass.equals(eventValueInfo.valueClass)) {
                continue;
            }
            if (!checkExcludes(eventValueInfo, eventClass)) {
                return null;
            }
            if (eventValueInfo.eventClass.isAssignableFrom(eventClass)) {
                @SuppressWarnings("unchecked")
                Converter<? super E, ? extends T> converter =
                        (Converter<? super E, ? extends T>) eventValueInfo.converter;
                return converter;
            }
        }
        return null;
    }

    public static <T, E extends Event> Kleenean hasMultipleConverters(Class<E> eventClass, Class<T> valueClass, int time) {
        List<Converter<? super E, ? extends T>> converters = getEventValueConverters(eventClass, valueClass, time, true, false);
        if (converters == null) {
            return Kleenean.UNKNOWN;
        }
        return Kleenean.get(converters.size() > 1);
    }

    public static <T, E extends Event> @Nullable Converter<? super E, ? extends T> getEventValueConverter(
            Class<E> eventClass,
            Class<T> valueClass,
            int time
    ) {
        return getEventValueConverter(eventClass, valueClass, time, true);
    }

    private static <T, E extends Event> @Nullable Converter<? super E, ? extends T> getEventValueConverter(
            Class<E> eventClass,
            Class<T> valueClass,
            int time,
            boolean allowDefault
    ) {
        List<Converter<? super E, ? extends T>> list = getEventValueConverters(eventClass, valueClass, time, allowDefault);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private static <T, E extends Event> @Nullable List<Converter<? super E, ? extends T>> getEventValueConverters(
            Class<E> eventClass,
            Class<T> valueClass,
            int time,
            boolean allowDefault
    ) {
        return getEventValueConverters(eventClass, valueClass, time, allowDefault, true);
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Event> @Nullable List<Converter<? super E, ? extends T>> getEventValueConverters(
            Class<E> eventClass,
            Class<T> valueClass,
            int time,
            boolean allowDefault,
            boolean allowConverting
    ) {
        List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
        List<Converter<? super E, ? extends T>> list = new ArrayList<>();
        Converter<? super E, ? extends T> exact = getExactEventValueConverter(eventClass, valueClass, time);
        if (exact != null) {
            list.add(exact);
            return list;
        }
        Map<EventValueInfo<?, ?>, Converter<? super E, ? extends T>> infoConverterMap = new HashMap<>();
        for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
            if (!valueClass.isAssignableFrom(eventValueInfo.valueClass)) {
                continue;
            }
            if (!checkExcludes(eventValueInfo, eventClass)) {
                return null;
            }
            if (eventValueInfo.eventClass.isAssignableFrom(eventClass)) {
                Converter<? super E, ? extends T> converter =
                        (Converter<? super E, ? extends T>) eventValueInfo.converter;
                list.add(converter);
                infoConverterMap.put(eventValueInfo, converter);
                continue;
            }
            if (!eventClass.isAssignableFrom(eventValueInfo.eventClass)) {
                continue;
            }
            Converter<? super E, ? extends T> converter = event -> {
                if (!eventValueInfo.eventClass.isInstance(event)) {
                    return null;
                }
                return ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(event);
            };
            list.add(converter);
            infoConverterMap.put(eventValueInfo, converter);
        }
        if (!list.isEmpty()) {
            return stripConverters(valueClass, infoConverterMap, list);
        }
        if (!allowConverting) {
            return null;
        }
        for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
            if (!eventValueInfo.valueClass.isAssignableFrom(valueClass)) {
                continue;
            }
            boolean checkInstanceOf = !eventValueInfo.eventClass.isAssignableFrom(eventClass);
            if (checkInstanceOf && !eventClass.isAssignableFrom(eventValueInfo.eventClass)) {
                continue;
            }
            if (!checkExcludes(eventValueInfo, eventClass)) {
                return null;
            }
            Converter<? super E, ? extends T> converter = event -> {
                if (checkInstanceOf && !eventValueInfo.eventClass.isInstance(event)) {
                    return null;
                }
                T object = ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(event);
                if (valueClass.isInstance(object)) {
                    return object;
                }
                return null;
            };
            list.add(converter);
            infoConverterMap.put(eventValueInfo, converter);
        }
        if (!list.isEmpty()) {
            return stripConverters(valueClass, infoConverterMap, list);
        }
        for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
            if (!eventClass.equals(eventValueInfo.eventClass)) {
                continue;
            }
            Converter<? super E, ? extends T> converter =
                    (Converter<? super E, ? extends T>) getConvertedConverter(eventValueInfo, valueClass, false);
            if (converter == null) {
                continue;
            }
            if (!checkExcludes(eventValueInfo, eventClass)) {
                return null;
            }
            list.add(converter);
        }
        if (!list.isEmpty()) {
            return list;
        }
        for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
            if (!eventClass.isAssignableFrom(eventValueInfo.eventClass)) {
                continue;
            }
            Converter<? super E, ? extends T> converter =
                    (Converter<? super E, ? extends T>) getConvertedConverter(eventValueInfo, valueClass, true);
            if (converter == null) {
                continue;
            }
            if (!checkExcludes(eventValueInfo, eventClass)) {
                return null;
            }
            list.add(converter);
        }
        if (!list.isEmpty()) {
            return list;
        }
        if (allowDefault && time != TIME_NOW) {
            return getEventValueConverters(eventClass, valueClass, TIME_NOW, false);
        }
        return null;
    }

    private static <E extends Event, T> List<Converter<? super E, ? extends T>> stripConverters(
            Class<T> valueClass,
            Map<EventValueInfo<?, ?>, Converter<? super E, ? extends T>> infoConverterMap,
            List<Converter<? super E, ? extends T>> converters
    ) {
        if (converters.size() == 1) {
            return converters;
        }
        ClassInfo<T> valueClassInfo = Classes.getExactClassInfo(valueClass);
        List<Converter<? super E, ? extends T>> stripped = new ArrayList<>();
        for (EventValueInfo<?, ?> eventValueInfo : infoConverterMap.keySet()) {
            ClassInfo<?> thisClassInfo = Classes.getExactClassInfo(eventValueInfo.valueClass);
            if (thisClassInfo != null && !thisClassInfo.equals(valueClassInfo)) {
                continue;
            }
            stripped.add(infoConverterMap.get(eventValueInfo));
        }
        if (stripped.isEmpty()) {
            return converters;
        }
        return stripped;
    }

    private static boolean checkExcludes(EventValueInfo<?, ?> info, Class<? extends Event> eventClass) {
        if (info.excludes == null) {
            return true;
        }
        for (Class<? extends Event> exclude : info.excludes) {
            if (exclude.isAssignableFrom(eventClass)) {
                Skript.error(info.excludeErrorMessage);
                return false;
            }
        }
        return true;
    }

    private static <E extends Event, F, T> @Nullable Converter<? super E, ? extends T> getConvertedConverter(
            EventValueInfo<E, F> info,
            Class<T> valueClass,
            boolean checkInstanceOf
    ) {
        Converter<? super F, ? extends T> converter = Converters.getConverter(info.valueClass, valueClass);
        if (converter == null) {
            return null;
        }
        return event -> {
            if (checkInstanceOf && !info.eventClass.isInstance(event)) {
                return null;
            }
            F value = info.converter.convert(event);
            if (value == null) {
                return null;
            }
            return converter.convert(value);
        };
    }

    public static boolean doesExactEventValueHaveTimeStates(Class<? extends Event> eventClass, Class<?> valueClass) {
        return getExactEventValueConverter(eventClass, valueClass, TIME_PAST) != null
                || getExactEventValueConverter(eventClass, valueClass, TIME_FUTURE) != null;
    }

    public static boolean doesEventValueHaveTimeStates(Class<? extends Event> eventClass, Class<?> valueClass) {
        return getEventValueConverter(eventClass, valueClass, TIME_PAST, false) != null
                || getEventValueConverter(eventClass, valueClass, TIME_FUTURE, false) != null;
    }

    public static int[] getTimeStates() {
        return new int[]{TIME_PAST, TIME_NOW, TIME_FUTURE};
    }

    public static Multimap<Class<? extends Event>, EventValueInfo<?, ?>> getPerEventEventValues() {
        Multimap<Class<? extends Event>, EventValueInfo<?, ?>> eventValues = MultimapBuilder
                .hashKeys()
                .hashSetValues()
                .build();
        for (int time : getTimeStates()) {
            for (EventValueInfo<?, ?> eventValueInfo : getEventValuesListForTime(time)) {
                Collection<EventValueInfo<?, ?>> existing = eventValues.get(eventValueInfo.eventClass);
                existing.add(eventValueInfo);
                eventValues.putAll(eventValueInfo.eventClass, existing);
            }
        }
        return eventValues;
    }

    public record EventValueInfo<E extends Event, T>(
            Class<E> eventClass,
            Class<T> valueClass,
            Converter<E, T> converter,
            @Nullable String excludeErrorMessage,
            @Nullable Class<? extends E>[] excludes,
            int time
    ) {
        public EventValueInfo {
            assert eventClass != null;
            assert valueClass != null;
            assert converter != null;
        }
    }
}
