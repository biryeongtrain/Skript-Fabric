package ch.njol.skript.util.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonPrimitive;
import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatSupportCompatibilityTest {

    @BeforeEach
    void setUpLanguage() {
        Language.clear();
        Language.loadDefault(defaultEntries());
        ChatMessages.registerListeners();
        ChatMessages.linkParseMode = LinkParseMode.DISABLED;
        ChatMessages.colorResetCodes = false;
    }

    @AfterEach
    void resetLanguage() {
        Language.clear();
        ChatMessages.linkParseMode = LinkParseMode.DISABLED;
        ChatMessages.colorResetCodes = false;
        Language.loadDefault(defaultEntries());
    }

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

    @Test
    void parseUnderstandsTagsLegacyCodesHexLinksAndJson() {
        List<MessageComponent> parsed = ChatMessages.parse("<light_red>Hello <bold>world <#12AbEf>hex https://skriptlang.org");

        assertEquals("", parsed.get(0).text);
        assertEquals("Hello ", parsed.get(1).text);
        assertEquals("red", parsed.get(1).color);
        assertTrue(parsed.get(2).bold);
        assertEquals("world ", parsed.get(2).text);
        assertEquals("#12abef", parsed.get(3).color);
        assertEquals("hex https://skriptlang.org", parsed.get(3).text);

        MessageComponent[] array = ChatMessages.parseToArray("&cHi");
        assertEquals(2, array.length);
        assertEquals("red", array[1].color);
        assertEquals("Hi", array[1].text);

        String json = ChatMessages.toJson("<run_command:/test>go");
        assertTrue(json.contains("\"clickEvent\""));
        assertTrue(json.contains("/test"));
    }

    @Test
    void linkParsingAndStyleHelpersMatchUpstreamUtilities() {
        ChatMessages.linkParseMode = LinkParseMode.LENIENT;
        List<MessageComponent> parsed = ChatMessages.parse("Visit example.com now");

        assertEquals("Visit ", parsed.get(0).text);
        assertEquals("example.com", parsed.get(1).text);
        assertEquals(MessageComponent.ClickEvent.Action.open_url, parsed.get(1).clickEvent.action);
        assertEquals("https://example.com", parsed.get(1).clickEvent.value);
        assertEquals(" now", parsed.get(2).text);

        MessageComponent first = new MessageComponent();
        first.color = "gold";
        first.bold = true;
        first.font = "uniform";
        MessageComponent second = new MessageComponent();
        ChatMessages.copyStyles(first, second);
        assertEquals("gold", second.color);
        assertTrue(second.bold);
        assertEquals("uniform", second.font);

        MessageComponent third = new MessageComponent();
        MessageComponent[] chain = new MessageComponent[]{first, third};
        ChatMessages.shareStyles(chain);
        assertEquals("gold", third.color);

        assertEquals("plain", ChatMessages.plainText("plain").text);
        assertEquals("Hello world", ChatMessages.stripStyles("<light_red>Hello &lworld"));
    }

    @Test
    void parsedStringAndAddonRegistrationWorkWithCurrentChatModel() {
        List<MessageComponent> parsed = ChatMessages.fromParsedString("§x§1§2§A§B§E§Fhex");
        assertEquals(2, parsed.size());
        assertEquals("#12abef", parsed.get(1).color);
        assertEquals("hex", parsed.get(1).text);

        ChatCode addonCode = new ChatCode() {
            @Override
            public void updateComponent(MessageComponent component, String param) {
                component.font = param.toUpperCase();
            }

            @Override
            public boolean hasParam() {
                return true;
            }

            @Override
            public String getColorCode() {
                return null;
            }

            @Override
            public String getLangName() {
                return "wave";
            }

            @Override
            public boolean isLocalized() {
                return false;
            }

            @Override
            public char getColorChar() {
                return 0;
            }
        };

        ChatMessages.registerAddonCode(Skript.getAddonInstance(), addonCode);
        List<MessageComponent> addonParsed = ChatMessages.parse("<wave:ok>");
        assertEquals("OK", addonParsed.get(1).font);
        assertNotNull(ChatMessages.codes.get("wave"));
    }

    private static Map<String, String> defaultEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("colors.black.names", "black");
        entries.put("colors.dark_blue.names", "dark_blue");
        entries.put("colors.dark_green.names", "dark_green");
        entries.put("colors.dark_aqua.names", "dark_aqua");
        entries.put("colors.dark_red.names", "dark_red");
        entries.put("colors.dark_purple.names", "dark_purple");
        entries.put("colors.gold.names", "gold");
        entries.put("colors.gray.names", "gray, light_grey");
        entries.put("colors.dark_gray.names", "dark_gray");
        entries.put("colors.blue.names", "blue");
        entries.put("colors.green.names", "green");
        entries.put("colors.aqua.names", "aqua");
        entries.put("colors.light_red.names", "red, light_red");
        entries.put("colors.light_purple.names", "light_purple");
        entries.put("colors.yellow.names", "yellow");
        entries.put("colors.white.names", "white");
        entries.put("chat styles.bold", "bold");
        entries.put("chat styles.italic", "italic");
        entries.put("chat styles.underlined", "underlined, underline");
        entries.put("chat styles.strikethrough", "strikethrough");
        entries.put("chat styles.obfuscated", "obfuscated, magic");
        entries.put("chat styles.open_url", "open_url");
        entries.put("chat styles.run_command", "run_command");
        entries.put("chat styles.suggest_command", "suggest_command");
        entries.put("chat styles.change_page", "change_page");
        entries.put("chat styles.copy_to_clipboard", "copy_to_clipboard");
        entries.put("chat styles.show_text", "show_text");
        entries.put("chat styles.font", "font");
        entries.put("chat styles.insertion", "insertion");
        entries.put("chat styles.translate", "translate");
        entries.put("chat styles.keybind", "keybind");
        return entries;
    }
}
