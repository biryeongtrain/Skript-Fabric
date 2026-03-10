package ch.njol.skript.util.chat;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

/**
 * Handles parsing chat messages.
 */
public final class ChatMessages {

    public static LinkParseMode linkParseMode = LinkParseMode.DISABLED;
    public static boolean colorResetCodes = false;

    static final Map<String, ChatCode> codes = new HashMap<>();
    static final Set<ChatCode> addonCodes = new HashSet<>();
    static final ChatCode[] colorChars = new ChatCode[256];
    static final Pattern linkPattern = Pattern.compile(
            "[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"
    );

    static final Gson gson;
    private static boolean listenersRegistered = false;

    private ChatMessages() {
    }

    public static void registerListeners() {
        if (listenersRegistered) {
            return;
        }
        listenersRegistered = true;
        Language.addListener(() -> {
            codes.clear();
            Arrays.fill(colorChars, null);

            Skript.debug("Parsing message style lang files");
            for (SkriptChatCode code : SkriptChatCode.values()) {
                registerChatCode(code);
            }

            for (ChatCode code : addonCodes) {
                registerChatCode(code);
            }

            addColorChar('k', SkriptChatCode.obfuscated);
            addColorChar('l', SkriptChatCode.bold);
            addColorChar('m', SkriptChatCode.strikethrough);
            addColorChar('n', SkriptChatCode.underlined);
            addColorChar('o', SkriptChatCode.italic);
            addColorChar('r', SkriptChatCode.reset);
        });
    }

    static void registerChatCode(ChatCode code) {
        String langName = code.getLangName();
        if (langName == null) {
            return;
        }

        if (code.isLocalized()) {
            if (code.getColorCode() != null) {
                for (String name : Language.getList("colors." + langName + ".names")) {
                    codes.put(name, code);
                }
            } else {
                for (String name : Language.getList("chat styles." + langName)) {
                    codes.put(name, code);
                }
            }
        } else {
            codes.put(langName, code);
        }

        if (code.getColorChar() != 0) {
            addColorChar(code.getColorChar(), code);
        }
    }

    static void addColorChar(char code, ChatCode data) {
        colorChars[code] = data;
        colorChars[Character.toUpperCase(code)] = data;
    }

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(boolean.class, new MessageComponent.BooleanSerializer())
                .create();
    }

    private static class ComponentList {

        @Deprecated(since = "2.3.0", forRemoval = true)
        public String text = "";
        public @Nullable List<MessageComponent> extra;

        ComponentList(List<MessageComponent> components) {
            this.extra = components;
        }

        ComponentList(MessageComponent[] components) {
            this.extra = Arrays.asList(components);
        }
    }

    public static List<MessageComponent> parse(String msg) {
        char[] chars = msg.toCharArray();

        List<MessageComponent> components = new ArrayList<>();
        MessageComponent current = new MessageComponent();
        components.add(current);
        StringBuilder curStr = new StringBuilder();
        boolean lastWasColor = true;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            ChatCode code = null;
            String param = "";

            if (c == '<') {
                int end = -1;
                int angleBrackets = 1;
                for (int j = i + 1; j < chars.length; j++) {
                    char c2 = chars[j];
                    if (c2 == '<') {
                        angleBrackets++;
                    } else if (c2 == '>') {
                        angleBrackets--;
                    }
                    if (angleBrackets == 0) {
                        end = j;
                        break;
                    }
                }

                if (end != -1) {
                    String tag = msg.substring(i + 1, end);
                    String name;
                    if (tag.contains(":")) {
                        String[] split = tag.split(":", 2);
                        name = split[0];
                        param = split[1];
                    } else {
                        name = tag;
                    }
                    name = name.toLowerCase(Locale.ENGLISH);

                    boolean tryHex = name.startsWith("#");
                    String chatColor = tryHex ? Utils.parseHexColor(name) : null;
                    tryHex = chatColor != null;

                    code = codes.get(name);
                    if (code != null || tryHex) {
                        current.text = curStr.toString();
                        curStr = new StringBuilder();

                        MessageComponent old = current;
                        current = new MessageComponent();
                        components.add(current);

                        if (tryHex) {
                            current.color = chatColor;
                        } else if (code != null && code.getColorCode() != null) {
                            current.color = code.getColorCode();
                        } else if (code != null) {
                            code.updateComponent(current, param);
                        }

                        copyStyles(old, current);
                        i = end;
                        lastWasColor = true;
                        continue;
                    }
                }
            } else if (c == '&' || c == '§') {
                if (i == chars.length - 1) {
                    curStr.append(c);
                    continue;
                }

                char color = chars[i + 1];
                boolean tryHex = color == 'x';
                String chatColor = null;
                if (tryHex && i + 14 < chars.length) {
                    chatColor = Utils.parseHexColor(msg.substring(i + 2, i + 14).replace("&", "").replace("§", ""));
                    tryHex = chatColor != null;
                }

                if (color >= colorChars.length) {
                    curStr.append(c);
                    continue;
                }
                code = colorChars[color];
                if (code == null && !tryHex) {
                    curStr.append(c).append(color);
                } else {
                    current.text = curStr.toString();
                    curStr = new StringBuilder();

                    MessageComponent old = current;
                    current = new MessageComponent();
                    components.add(current);

                    if (tryHex) {
                        current.color = chatColor;
                        i += 12;
                    } else if (code != null && code.getColorCode() != null) {
                        current.color = code.getColorCode();
                    } else if (code != null) {
                        code.updateComponent(current, param);
                    }

                    copyStyles(old, current);
                }

                i++;
                lastWasColor = true;
                continue;
            }

            if ((linkParseMode == LinkParseMode.STRICT || linkParseMode == LinkParseMode.LENIENT) && c == 'h') {
                String rest = msg.substring(i);
                String link = null;
                if (rest.startsWith("http://") || rest.startsWith("https://")) {
                    link = rest.split(" ", 2)[0];
                }

                if (link != null && !link.isEmpty()) {
                    current.text = curStr.toString();
                    curStr = new StringBuilder();

                    MessageComponent old = current;
                    current = new MessageComponent();
                    copyStyles(old, current);
                    components.add(current);

                    SkriptChatCode.open_url.updateComponent(current, link);
                    current.text = link;

                    i += link.length() - 1;

                    current = new MessageComponent();
                    components.add(current);
                    continue;
                }
            } else if (linkParseMode == LinkParseMode.LENIENT && (lastWasColor || i == 0 || chars[i - 1] == ' ')) {
                String rest = msg.substring(i);
                String potentialLink = rest.split(" ", 2)[0];
                String link = linkPattern.matcher(potentialLink).matches() ? potentialLink : null;

                if (link != null && !link.isEmpty()) {
                    String url = link.startsWith("http://") || link.startsWith("https://") ? link : "https://" + link;

                    current.text = curStr.toString();
                    curStr = new StringBuilder();

                    MessageComponent old = current;
                    current = new MessageComponent();
                    copyStyles(old, current);
                    components.add(current);

                    SkriptChatCode.open_url.updateComponent(current, url);
                    current.text = link;

                    i += link.length() - 1;

                    current = new MessageComponent();
                    components.add(current);
                    continue;
                }
            }

            curStr.append(c);
            lastWasColor = false;
        }

        current.text = curStr.toString();
        return components;
    }

    public static MessageComponent[] parseToArray(String msg) {
        return parse(msg).toArray(new MessageComponent[0]);
    }

    public static List<MessageComponent> fromParsedString(String msg) {
        char[] chars = msg.toCharArray();

        List<MessageComponent> components = new ArrayList<>();
        MessageComponent current = new MessageComponent();
        components.add(current);
        StringBuilder curStr = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c != '§') {
                curStr.append(c);
                continue;
            }
            if (i == chars.length - 1) {
                curStr.append(c);
                continue;
            }

            char color = chars[i + 1];
            boolean tryHex = color == 'x';
            String chatColor = null;
            if (tryHex && i + 14 < chars.length) {
                chatColor = Utils.parseHexColor(msg.substring(i + 2, i + 14).replace("&", "").replace("§", ""));
                tryHex = chatColor != null;
            }

            if (color >= colorChars.length) {
                curStr.append(c);
                continue;
            }

            ChatCode code = colorChars[color];
            if (code == null && !tryHex) {
                curStr.append(c).append(color);
            } else {
                current.text = curStr.toString();
                curStr = new StringBuilder();

                MessageComponent old = current;
                current = new MessageComponent();
                components.add(current);

                if (tryHex) {
                    current.color = chatColor;
                    i += 12;
                } else if (code != null && code.getColorCode() != null) {
                    current.color = code.getColorCode();
                } else if (code != null) {
                    code.updateComponent(current, "");
                }

                copyStyles(old, current);
            }

            i++;
        }

        current.text = curStr.toString();
        return components;
    }

    public static String toJson(String msg) {
        return gson.toJson(new ComponentList(parse(msg)));
    }

    public static String toJson(List<MessageComponent> components) {
        return gson.toJson(new ComponentList(components));
    }

    public static void copyStyles(MessageComponent from, MessageComponent to) {
        if (to.reset) {
            return;
        }

        if (to.color == null || !colorResetCodes) {
            if (!to.bold) {
                to.bold = from.bold;
            }
            if (!to.italic) {
                to.italic = from.italic;
            }
            if (!to.underlined) {
                to.underlined = from.underlined;
            }
            if (!to.strikethrough) {
                to.strikethrough = from.strikethrough;
            }
            if (!to.obfuscated) {
                to.obfuscated = from.obfuscated;
            }
            if (to.color == null) {
                to.color = from.color;
            }
        }

        if (to.clickEvent == null) {
            to.clickEvent = from.clickEvent;
        }
        if (to.insertion == null) {
            to.insertion = from.insertion;
        }
        if (to.hoverEvent == null) {
            to.hoverEvent = from.hoverEvent;
        }
        if (to.font == null) {
            to.font = from.font;
        }
    }

    public static void shareStyles(MessageComponent[] components) {
        MessageComponent previous = null;
        for (MessageComponent component : components) {
            if (previous != null) {
                copyStyles(previous, component);
            }
            previous = component;
        }
    }

    public static MessageComponent plainText(String str) {
        MessageComponent component = new MessageComponent();
        component.text = str;
        return component;
    }

    public static void registerAddonCode(@Nullable SkriptAddon addon, @Nullable ChatCode code) {
        Objects.requireNonNull(addon);
        Objects.requireNonNull(code);

        addonCodes.add(code);
        registerChatCode(code);
    }

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("[§&]x");
    private static final Pattern ANY_COLOR_PATTERN = Pattern.compile("(?i)[&§][0-9a-folkrnm]");

    public static String stripStyles(String text) {
        String previous;
        String result = text;
        do {
            previous = result;

            List<MessageComponent> components = parse(result);
            StringBuilder builder = new StringBuilder();
            for (MessageComponent component : components) {
                if (component.translation != null) {
                    builder.append(component.translation);
                }
                if (component.keybind != null) {
                    builder.append(component.keybind);
                }
                builder.append(component.text);
            }
            String plain = builder.toString();
            plain = HEX_COLOR_PATTERN.matcher(plain).replaceAll("");
            result = ANY_COLOR_PATTERN.matcher(plain).replaceAll("");
        } while (!previous.equals(result));

        return result;
    }
}
