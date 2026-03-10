package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Replace")
@Description(
        "Replaces all occurrences of a given text or regex with another text. Please note that you can only change "
                + "variables and a few expressions, e.g. a <a href='/#ExprMessage'>message</a> or a line of a sign."
)
@Example("replace \"<item>\" in {_msg} with \"[%name of player's tool%]\"")
@Example("replace every \"&\" with \"\\u00A7\" in line 1 of targeted block")
@Example("""
        # Very simple chat censor
        on chat:
            replace all "idiot" and "noob" with "****" in the message
            regex replace "\\b(idiot|noob)\\b" with "****" in the message # Regex version using word boundaries for better results
        """)
@Example("replace all stone and dirt in player's inventory and player's top inventory with diamond")
@Since("2.0, 2.2-dev24 (multiple strings, items in inventory), 2.5 (replace first, case sensitivity), 2.10 (regex)")
public final class EffReplace extends Effect {

    private static boolean registered;

    private Expression<?> haystack;
    private Expression<?> needles;
    private Expression<?> replacement;
    private boolean replaceString;
    private boolean replaceRegex;
    private boolean replaceFirst;
    private boolean caseSensitive;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffReplace.class,
                "replace [(all|every)|first:[the] first] %strings% in %strings% with %string% [case:with case sensitivity]",
                "replace [(all|every)|first:[the] first] %strings% with %string% in %strings% [case:with case sensitivity]",
                "(replace [with|using] regex|regex replace) %strings% in %strings% with %string%",
                "(replace [with|using] regex|regex replace) %strings% with %string% in %strings%",
                "replace [all|every] %itemtypes% in %inventories% with %itemtype%",
                "replace [all|every] %itemtypes% with %itemtype% in %inventories%"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        haystack = expressions[1 + matchedPattern % 2];
        replaceString = matchedPattern < 4;
        replaceFirst = parseResult.hasTag("first");
        replaceRegex = matchedPattern == 2 || matchedPattern == 3;

        if (replaceString && !ChangerUtils.acceptsChange(haystack, ChangeMode.SET, String.class)) {
            Skript.error(haystack + " cannot be changed and can thus not have parts replaced");
            return false;
        }

        if (parseResult.hasTag("case")) {
            caseSensitive = true;
        }

        needles = expressions[0];
        replacement = expressions[2 - matchedPattern % 2];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object[] resolvedNeedles = needles.getAll(event);
        if (haystack instanceof ExpressionList<?> list) {
            for (Expression<?> haystackExpr : list.getExpressions()) {
                replace(event, resolvedNeedles, haystackExpr);
            }
        } else {
            replace(event, resolvedNeedles, haystack);
        }
    }

    @SuppressWarnings("unchecked")
    private void replace(SkriptEvent event, Object[] resolvedNeedles, Expression<?> haystackExpr) {
        Object[] resolvedHaystack = haystackExpr.getAll(event);
        Object resolvedReplacement = replacement.getSingle(event);

        if (resolvedReplacement == null || resolvedHaystack == null || resolvedHaystack.length == 0
                || resolvedNeedles == null || resolvedNeedles.length == 0) {
            return;
        }

        if (replaceString) {
            Function<String, String> replaceFunction = getReplaceFunction(resolvedNeedles, (String) resolvedReplacement);
            ((Expression<String>) haystackExpr).changeInPlace(event, replaceFunction);
            return;
        }

        FabricItemType itemType = (FabricItemType) resolvedReplacement;
        for (FabricInventory inventory : (FabricInventory[]) resolvedHaystack) {
            Container container = inventory.container();
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                for (FabricItemType needle : (FabricItemType[]) resolvedNeedles) {
                    if (!needle.isOfType(stack)) {
                        continue;
                    }
                    ItemStack newStack = itemType.toStack();
                    newStack.setCount(stack.getCount());
                    container.setItem(slot, newStack);
                    break;
                }
            }
        }
    }

    private @NotNull Function<String, String> getReplaceFunction(Object[] resolvedNeedles, String resolvedReplacement) {
        if (replaceRegex) {
            List<Pattern> patterns = new ArrayList<>(resolvedNeedles.length);
            for (Object needle : resolvedNeedles) {
                try {
                    patterns.add(Pattern.compile((String) needle));
                } catch (Exception ignored) {
                }
            }
            return haystackString -> {
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(haystackString);
                    haystackString = replaceFirst
                            ? matcher.replaceFirst(resolvedReplacement)
                            : matcher.replaceAll(resolvedReplacement);
                }
                return haystackString;
            };
        }
        if (replaceFirst) {
            return haystackString -> {
                for (Object needle : resolvedNeedles) {
                    haystackString = StringUtils.replaceFirst(
                            haystackString,
                            (String) needle,
                            Matcher.quoteReplacement(resolvedReplacement),
                            caseSensitive
                    );
                }
                return haystackString;
            };
        }
        return haystackString -> {
            for (Object needle : resolvedNeedles) {
                haystackString = StringUtils.replace(haystackString, (String) needle, resolvedReplacement, caseSensitive);
            }
            return haystackString;
        };
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("replace");
        if (replaceFirst) {
            builder.append("the first");
        }
        if (replaceRegex) {
            builder.append("regex");
        }
        builder.append(needles, "in", haystack, "with", replacement);
        if (caseSensitive) {
            builder.append("with case sensitivity");
        }
        return builder.toString();
    }
}
