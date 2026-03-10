package ch.njol.skript.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import java.lang.reflect.Field;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;

final class EventCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        EntityData.register();
        EntityType.register();
        EvtDamage.register();
        EvtBreeding.register();
        EvtBucketCatch.register();
        EvtScript.register();
        EvtSkript.register();
    }

    @Test
    void damageEventParsesEntityFilters() throws Exception {
        EvtDamage event = parseEvent("damage of zombie by skeleton", EvtDamage.class);

        assertEquals("zombie", readLiteralArray(event, "ofTypes"));
        assertEquals("skeleton", readLiteralArray(event, "byTypes"));
        assertEquals("damage of [zombie] by [skeleton]", event.toString(null, false));
        assertEquals(1, event.getEventClasses().length);
    }

    @Test
    void breedingEventParsesPluralEntityDataFilter() throws Exception {
        EvtBreeding event = parseEvent("entity breeding of cows", EvtBreeding.class);

        assertEquals("cow", readArray(event, "entityTypes"));
        assertEquals("breeding of cow", event.toString(null, false));
    }

    @Test
    void bucketCatchEventParsesEntityDataFilter() throws Exception {
        EvtBucketCatch event = parseEvent("bucket catching of axolotls", EvtBucketCatch.class);

        assertEquals("axolotl", readArray(event, "entityTypes"));
        assertEquals("bucket catch of axolotl", event.toString(null, false));
    }

    @Test
    void scriptEventParsesAsyncLoadPattern() {
        EvtScript event = parseEvent("on async script load", EvtScript.class);

        assertEquals("async script load", event.toString(null, false));
    }

    @Test
    void skriptEventParsesServerStartPattern() {
        EvtSkript event = parseEvent("on server start", EvtSkript.class);

        assertEquals("on skript start", event.toString(null, false));
    }

    private <T> T parseEvent(String input, Class<T> type) {
        ch.njol.skript.lang.SkriptEvent parsed = ch.njol.skript.lang.SkriptEvent.parse(
                input,
                new SectionNode(input),
                "failed"
        );
        assertNotNull(parsed);
        assertInstanceOf(type, parsed);
        return type.cast(parsed);
    }

    private String readLiteralArray(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        assertInstanceOf(ch.njol.skript.lang.Literal.class, value);
        Object[] array = ((ch.njol.skript.lang.Literal<?>) value).getArray(null);
        return array.length == 0 ? "" : String.valueOf(array[0]);
    }

    private String readArray(Object owner, String fieldName) throws Exception {
        Object value = readField(owner, fieldName);
        Object[] array = (Object[]) value;
        return array.length == 0 ? "" : String.valueOf(array[0]);
    }

    private Object readField(Object owner, String fieldName) throws Exception {
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(owner);
    }
}
