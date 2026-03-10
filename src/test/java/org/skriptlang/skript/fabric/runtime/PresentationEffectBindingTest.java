package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

final class PresentationEffectBindingTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void actionBarFixtureBindsPlayerRecipient() throws Exception {
        ch.njol.skript.effects.EffActionBar effect = loadFirstEffect(
                "skript/gametest/effect/send_action_bar_names_player.sk",
                ch.njol.skript.effects.EffActionBar.class
        );
        assertEquals("event-player", expression(effect, "recipients").toString(null, false));
    }

    @Test
    void broadcastFixtureBindsMessageExpression() throws Exception {
        ch.njol.skript.effects.EffBroadcast effect = loadFirstEffect(
                "skript/gametest/effect/broadcast_message_marks_block.sk",
                ch.njol.skript.effects.EffBroadcast.class
        );
        assertEquals("\"hello from lane f\"", expression(effect, "messageExpr").toString(null, false));
    }

    @Test
    void kickFixtureBindsPlayerAndReason() throws Exception {
        ch.njol.skript.effects.EffKick effect = loadFirstEffect(
                "skript/gametest/effect/kick_event_player_names_player.sk",
                ch.njol.skript.effects.EffKick.class
        );
        assertEquals("event-player", expression(effect, "players").toString(null, false));
        assertEquals("\"lane f kick\"", expression(effect, "reason").toString(null, false));
    }

    @Test
    void messageFixtureBindsPlayerRecipientAndSender() throws Exception {
        ch.njol.skript.effects.EffMessage effect = loadFirstEffect(
                "skript/gametest/effect/send_message_names_player.sk",
                ch.njol.skript.effects.EffMessage.class
        );
        assertEquals("event-player", expression(effect, "recipients").toString(null, false));
        assertEquals("event-player", expression(effect, "sender").toString(null, false));
    }

    @Test
    void playSoundFixtureBindsPlayerRecipient() throws Exception {
        ch.njol.skript.effects.EffPlaySound effect = loadFirstEffect(
                "skript/gametest/effect/play_sound_names_player.sk",
                ch.njol.skript.effects.EffPlaySound.class
        );
        assertEquals("event-player", expression(effect, "players").toString(null, false));
        assertNotNull(expression(effect, "sounds"));
    }

    @Test
    void resetTitleFixtureBindsPlayerRecipient() throws Exception {
        ch.njol.skript.effects.EffResetTitle effect = loadFirstEffect(
                "skript/gametest/effect/reset_title_names_player.sk",
                ch.njol.skript.effects.EffResetTitle.class
        );
        assertEquals("event-player", expression(effect, "recipients").toString(null, false));
    }

    @Test
    void sendResourcePackFixtureBindsUrlHashAndRecipient() throws Exception {
        ch.njol.skript.effects.EffSendResourcePack effect = loadFirstEffect(
                "skript/gametest/effect/send_resource_pack_names_player.sk",
                ch.njol.skript.effects.EffSendResourcePack.class
        );
        assertEquals("event-player", expression(effect, "recipients").toString(null, false));
        assertEquals(
                "\"https://example.com/pack.zip\" with hash \"0123456789012345678901234567890123456789\"",
                expression(effect, "url").toString(null, false)
        );
    }

    @Test
    void sendTitleFixtureBindsPlayerRecipientAndTimes() throws Exception {
        ch.njol.skript.effects.EffSendTitle effect = loadFirstEffect(
                "skript/gametest/effect/send_title_names_player.sk",
                ch.njol.skript.effects.EffSendTitle.class
        );
        assertEquals("event-player", expression(effect, "recipients").toString(null, false));
        assertEquals("[5 seconds]", expression(effect, "stay").toString(null, false));
    }

    @Test
    void stopSoundFixtureTracksSpecificSoundMode() throws Exception {
        ch.njol.skript.effects.EffStopSound effect = loadFirstEffect(
                "skript/gametest/effect/stop_sound_names_player.sk",
                ch.njol.skript.effects.EffStopSound.class
        );
        assertFalse(readBoolean(effect, "allSounds"));
        assertEquals("event-player", expression(effect, "players").toString(null, false));
    }

    private <T> T loadFirstEffect(String resourcePath, Class<T> effectClass) throws Exception {
        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.clearScripts();
        runtime.loadFromPath(Path.of("src/gametest/resources").resolve(resourcePath));
        Trigger trigger = onlyLoadedTrigger(runtime);
        Object first = firstTriggerItem(trigger);
        assertInstanceOf(effectClass, first);
        return effectClass.cast(first);
    }

    private Trigger onlyLoadedTrigger(SkriptRuntime runtime) throws ReflectiveOperationException {
        Field scriptsField = SkriptRuntime.class.getDeclaredField("scripts");
        scriptsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Script> scripts = (List<Script>) scriptsField.get(runtime);
        Script script = scripts.getFirst();
        Structure structure = script.getStructures().getFirst();
        return ((ch.njol.skript.lang.SkriptEvent) structure).getTrigger();
    }

    private Object firstTriggerItem(Trigger trigger) throws ReflectiveOperationException {
        Field firstField = TriggerSection.class.getDeclaredField("first");
        firstField.setAccessible(true);
        TriggerItem first = (TriggerItem) firstField.get(trigger);
        assertNotNull(first);
        return first;
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        Object value = field.get(owner);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private boolean readBoolean(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.getBoolean(owner);
    }

    private Field findField(Class<?> owner, String fieldName) throws NoSuchFieldException {
        Class<?> current = owner;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
