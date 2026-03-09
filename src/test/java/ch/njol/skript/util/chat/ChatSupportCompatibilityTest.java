package ch.njol.skript.util.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

class ChatSupportCompatibilityTest {

    @Test
    void colorAndFormattingCodesPopulateMessageComponents() {
        MessageComponent component = new MessageComponent();

        SkriptChatCode.red.updateComponent(component, "");
        SkriptChatCode.bold.updateComponent(component, "");
        SkriptChatCode.italic.updateComponent(component, "");

        assertEquals("red", component.color);
        assertTrue(component.bold);
        assertTrue(component.italic);
        assertFalse(component.reset);
    }

    @Test
    void clickHoverAndInsertionCodesStoreParameters() {
        MessageComponent component = new MessageComponent();

        SkriptChatCode.run_command.updateComponent(component, "/test");
        SkriptChatCode.show_text.updateComponent(component, "hover");
        SkriptChatCode.insertion.updateComponent(component, "insert");
        SkriptChatCode.translate.updateComponent(component, "chat.type.text");
        SkriptChatCode.keybind.updateComponent(component, "key.jump");
        SkriptChatCode.font.updateComponent(component, "uniform");

        assertEquals(MessageComponent.ClickEvent.Action.run_command, component.clickEvent.action);
        assertEquals("/test", component.clickEvent.value);
        assertEquals(MessageComponent.HoverEvent.Action.show_text, component.hoverEvent.action);
        assertEquals("hover", component.hoverEvent.value);
        assertEquals("insert", component.insertion);
        assertEquals("chat.type.text", component.translation);
        assertEquals("key.jump", component.keybind);
        assertEquals("uniform", component.font);
    }

    @Test
    void metadataHelpersMatchUpstreamShape() {
        assertEquals(LinkParseMode.STRICT, LinkParseMode.valueOf("STRICT"));
        assertTrue(SkriptChatCode.open_url.hasParam());
        assertEquals('c', SkriptChatCode.red.getColorChar());
        assertEquals("light_grey", SkriptChatCode.gray.getLangName());
        assertEquals("dark_aqua", SkriptChatCode.dark_aqua.getColorCode());
        assertTrue(SkriptChatCode.reset.isLocalized());
    }

    @Test
    void copyPreservesReferencesAndBooleanSerializerOmitsFalse() {
        MessageComponent component = new MessageComponent();
        component.text = "hello";
        component.color = "gold";
        component.clickEvent = new MessageComponent.ClickEvent(MessageComponent.ClickEvent.Action.change_page, "2");
        component.hoverEvent = new MessageComponent.HoverEvent(MessageComponent.HoverEvent.Action.show_text, "tip");

        MessageComponent copy = component.copy();

        assertEquals("hello", copy.text);
        assertEquals("gold", copy.color);
        assertSame(component.clickEvent, copy.clickEvent);
        assertSame(component.hoverEvent, copy.hoverEvent);

        MessageComponent.BooleanSerializer serializer = new MessageComponent.BooleanSerializer();
        assertEquals(new JsonPrimitive(true), serializer.serialize(true, Boolean.class, null));
        assertNull(serializer.serialize(false, Boolean.class, null));
        assertNull(serializer.serialize(null, Boolean.class, null));
    }
}
