package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import java.util.ArrayList;
import java.util.List;

public class ExprStringColor extends PropertyExpression<String, Object> {

    private enum StringColor {
        ALL,
        FIRST,
        LAST
    }

    static {
        Skript.registerExpression(
                ExprStringColor.class,
                Object.class,
                "[all [of the|the]|the] string colo[u]r[s] [code:code[s]] of %strings%",
                "[the] first string colo[u]r[s] [code:code[s]] of %strings%",
                "[the] last string colo[u]r[s] [code:code[s]] of %strings%"
        );
    }

    private StringColor selectedState;
    private boolean getCodes;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        selectedState = StringColor.values()[matchedPattern];
        getCodes = parseResult.hasTag("code");
        setExpr((Expression<String>) exprs[0]);
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event, String[] source) {
        List<Object> colors = new ArrayList<>();
        for (String string : source) {
            colors.addAll(readColors(ExprColoured.replaceChatStyles(string)));
        }
        if (selectedState == StringColor.LAST && !colors.isEmpty()) {
            return new Object[]{colors.get(colors.size() - 1)};
        }
        return colors.toArray(new Object[0]);
    }

    @Override
    public Class<?> getReturnType() {
        return getCodes ? String.class : Color.class;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return new Class[]{String.class, Color.class, ColorRGB.class};
    }

    @Override
    public boolean isSingle() {
        return selectedState != StringColor.ALL && getExpr().isSingle();
    }

    @Override
    public Expression<?> simplify() {
        if (getExpr() instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "string colors of " + getExpr().toString(event, debug);
    }

    private List<Object> readColors(String value) {
        List<Object> colors = new ArrayList<>();
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '§' || i + 1 >= value.length()) {
                continue;
            }
            char code = Character.toLowerCase(value.charAt(i + 1));
            if (code == 'x') {
                String legacyHexCode = readHexCode(value, i);
                if (legacyHexCode != null) {
                    colors.add(getCodes ? legacyHexCode : ColorRGB.parse(readHexValue(legacyHexCode)));
                    if (selectedState == StringColor.FIRST) {
                        return colors;
                    }
                    i += 13;
                }
                continue;
            }
            ColorRGB color = legacyColor(code);
            if (color == null) {
                continue;
            }
            colors.add(getCodes ? "§" + code : color);
            if (selectedState == StringColor.FIRST) {
                return colors;
            }
            i++;
        }
        return colors;
    }

    private static @Nullable String readHexCode(String value, int start) {
        if (start + 13 >= value.length()) {
            return null;
        }
        StringBuilder code = new StringBuilder("§x");
        for (int i = start + 2; i <= start + 12; i += 2) {
            if (value.charAt(i) != '§') {
                return null;
            }
            char digit = value.charAt(i + 1);
            if (Character.digit(digit, 16) == -1) {
                return null;
            }
            code.append('§').append(Character.toLowerCase(digit));
        }
        return code.toString();
    }

    private static String readHexValue(String legacyHexCode) {
        StringBuilder hex = new StringBuilder("#");
        for (int i = 3; i < legacyHexCode.length(); i += 2) {
            hex.append(legacyHexCode.charAt(i));
        }
        return hex.toString();
    }

    private static @Nullable ColorRGB legacyColor(char code) {
        return switch (code) {
            case '0' -> new ColorRGB(0, 0, 0);
            case '1' -> new ColorRGB(0, 0, 170);
            case '2' -> new ColorRGB(0, 170, 0);
            case '3' -> new ColorRGB(0, 170, 170);
            case '4' -> new ColorRGB(170, 0, 0);
            case '5' -> new ColorRGB(170, 0, 170);
            case '6' -> new ColorRGB(255, 170, 0);
            case '7' -> new ColorRGB(170, 170, 170);
            case '8' -> new ColorRGB(85, 85, 85);
            case '9' -> new ColorRGB(85, 85, 255);
            case 'a' -> new ColorRGB(85, 255, 85);
            case 'b' -> new ColorRGB(85, 255, 255);
            case 'c' -> new ColorRGB(255, 85, 85);
            case 'd' -> new ColorRGB(255, 85, 255);
            case 'e' -> new ColorRGB(255, 255, 85);
            case 'f' -> new ColorRGB(255, 255, 255);
            default -> null;
        };
    }
}
