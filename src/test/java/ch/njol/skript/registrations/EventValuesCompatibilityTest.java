package ch.njol.skript.registrations;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.experiments.QueueExperimentSyntax;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.LifeCycle;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.util.event.Event;

class EventValuesCompatibilityTest {

    @AfterEach
    void cleanup() throws ReflectiveOperationException {
        clearEventValues("DEFAULT_EVENT_VALUES");
        clearEventValues("FUTURE_EVENT_VALUES");
        clearEventValues("PAST_EVENT_VALUES");
        Classes.clearClassInfos();
    }

    @Test
    void registersSpecificEventValuesAheadOfBroaderEntries() {
        EventValues.registerEventValue(BaseEvent.class, Number.class, BaseEvent::number);
        EventValues.registerEventValue(ChildEvent.class, Integer.class, ChildEvent::integer);

        List<EventValues.EventValueInfo<?, ?>> infos = EventValues.getEventValuesListForTime(EventValues.TIME_NOW);

        assertEquals(2, infos.size());
        assertEquals(ChildEvent.class, infos.get(0).eventClass());
        assertEquals(Integer.class, infos.get(0).valueClass());
        assertEquals(BaseEvent.class, infos.get(1).eventClass());
        assertEquals(Number.class, infos.get(1).valueClass());
        assertEquals(7, EventValues.getEventValue(new ChildEvent(7), Integer.class, EventValues.TIME_NOW));
    }

    @Test
    void convertsEventValuesThroughRegisteredConvertersAndFallsBackToDefaultTime() {
        EventValues.registerEventValue(BaseEvent.class, ConvertibleNumber.class, BaseEvent::convertibleNumber);
        EventValues.registerEventValue(BaseEvent.class, String.class, BaseEvent::label, EventValues.TIME_NOW);
        ch.njol.skript.registrations.Converters.registerConverter(
                ConvertibleNumber.class,
                ConvertedText.class,
                value -> new ConvertedText("converted-" + value.value()),
                Converter.NO_LEFT_CHAINING
        );

        assertEquals(
                new ConvertedText("converted-4"),
                EventValues.getEventValue(new BaseEvent(4), ConvertedText.class, EventValues.TIME_NOW)
        );
        assertEquals(
                "label-4",
                EventValues.getEventValue(new BaseEvent(4), String.class, EventValues.TIME_FUTURE)
        );
        assertFalse(EventValues.doesEventValueHaveTimeStates(BaseEvent.class, String.class));
        assertFalse(EventValues.doesExactEventValueHaveTimeStates(BaseEvent.class, String.class));
    }

    @Test
    void stripsAssignableConvertersWhenExactClassInfoDiffers() {
        Classes.registerClassInfo(new ClassInfo<>(EntityValue.class, "entity"));
        Classes.registerClassInfo(new ClassInfo<>(PlayerValue.class, "player"));
        EventValues.registerEventValue(BaseEvent.class, EntityValue.class, event -> new EntityValue("entity"));
        EventValues.registerEventValue(BaseEvent.class, PlayerValue.class, event -> new PlayerValue("player"));

        assertEquals(Kleenean.FALSE, EventValues.hasMultipleConverters(BaseEvent.class, EntityValue.class, EventValues.TIME_NOW));

        Converter<? super BaseEvent, ? extends EntityValue> converter =
                EventValues.getEventValueConverter(BaseEvent.class, EntityValue.class, EventValues.TIME_NOW);

        assertNotNull(converter);
        assertEquals(new EntityValue("entity"), converter.convert(new BaseEvent(2)));
    }

    @Test
    void featureCompatibilityExposesExperimentPatternsAndRegistration() {
        Feature.registerAll(Skript.instance(), Skript.experiments());

        assertEquals("queues", Feature.QUEUES.codeName());
        assertEquals(LifeCycle.EXPERIMENTAL, Feature.QUEUES.phase());
        assertTrue(Feature.QUEUES.matches("queues"));
        assertTrue(Feature.SCRIPT_REFLECTION.matches("script reflection"));
        assertSame(Feature.QUEUES, Skript.experiments().find("queues"));
    }

    @Test
    void experimentSyntaxInterfacesRequireMatchingFeatureFlags() {
        QueueSyntax queueSyntax = new QueueSyntax();
        ReflectionSyntax reflectionSyntax = new ReflectionSyntax();

        assertEquals(Feature.QUEUES, queueSyntax.getExperimentData().getRequired().iterator().next());
        assertEquals(Feature.SCRIPT_REFLECTION, reflectionSyntax.getExperimentData().getRequired().iterator().next());
        assertFalse(queueSyntax.isSatisfiedBy(new ExperimentSet()));
        assertTrue(queueSyntax.isSatisfiedBy(experimentSetOf(Feature.QUEUES)));
        assertFalse(reflectionSyntax.isSatisfiedBy(new ExperimentSet()));
        assertTrue(reflectionSyntax.isSatisfiedBy(experimentSetOf(Feature.SCRIPT_REFLECTION)));
    }

    @Test
    void perEventViewGroupsRegisteredValuesByEventClass() {
        EventValues.registerEventValue(BaseEvent.class, Number.class, BaseEvent::number);
        EventValues.registerEventValue(ChildEvent.class, Integer.class, ChildEvent::integer, EventValues.TIME_FUTURE);

        var grouped = EventValues.getPerEventEventValues();

        assertEquals(1, grouped.get(BaseEvent.class).size());
        assertEquals(1, grouped.get(ChildEvent.class).size());
        assertArrayEquals(
                new int[]{EventValues.TIME_PAST, EventValues.TIME_NOW, EventValues.TIME_FUTURE},
                EventValues.getTimeStates()
        );
    }

    private static void clearEventValues(String fieldName) throws ReflectiveOperationException {
        Field field = EventValues.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ((List<?>) field.get(null)).clear();
    }

    private static ExperimentSet experimentSetOf(Experiment experiment) {
        ExperimentSet set = new ExperimentSet();
        set.add(experiment);
        return set;
    }

    private static final class QueueSyntax implements QueueExperimentSyntax {
        @Override
        public String toString(SkriptEvent event, boolean debug) {
            return "queue";
        }
    }

    private static final class ReflectionSyntax implements ReflectionExperimentSyntax {
        @Override
        public String toString(SkriptEvent event, boolean debug) {
            return "reflection";
        }
    }

    private static class BaseEvent implements Event {
        private final int value;

        BaseEvent(int value) {
            this.value = value;
        }

        Number number() {
            return value;
        }

        ConvertibleNumber convertibleNumber() {
            return new ConvertibleNumber(value);
        }

        String label() {
            return "label-" + value;
        }
    }

    private static final class ChildEvent extends BaseEvent {
        ChildEvent(int value) {
            super(value);
        }

        Integer integer() {
            return number().intValue();
        }
    }

    private record ConvertibleNumber(int value) {
    }

    private record ConvertedText(String text) {
    }

    private record EntityValue(String value) {
    }

    private record PlayerValue(String value) {
    }
}
