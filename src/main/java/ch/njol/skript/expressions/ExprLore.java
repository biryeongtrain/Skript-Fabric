package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLore extends SimpleExpression<String> {

    static {
        ch.njol.skript.Skript.registerExpression(
                ExprLore.class,
                String.class,
                "[the] lore of %itemstack/itemtype%",
                "%itemstack/itemtype%'[s] lore",
                "[the] line %number% of [the] lore of %itemstack/itemtype%",
                "[the] line %number% of %itemstack/itemtype%'[s] lore",
                "[the] %number%(st|nd|rd|th) line of [the] lore of %itemstack/itemtype%",
                "[the] %number%(st|nd|rd|th) line of %itemstack/itemtype%'[s] lore"
        );
    }

    private @Nullable Expression<Number> lineNumber;
    private Expression<?> item;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        lineNumber = exprs.length > 1 ? (Expression<Number>) exprs[0] : null;
        item = exprs[exprs.length - 1];
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        Object source = item.getSingle(event);
        Number requestedLine = lineNumber == null ? null : lineNumber.getSingle(event);
        if (lineNumber != null && requestedLine == null) {
            return null;
        }
        List<String> lore = readLore(source);
        if (requestedLine == null) {
            return lore.toArray(String[]::new);
        }
        int index = requestedLine.intValue() - 1;
        if (index < 0 || index >= lore.size()) {
            return new String[0];
        }
        return new String[]{lore.get(index)};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE -> new Class[]{String[].class, String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Object source = item.getSingle(event);
        if (source == null) {
            return;
        }

        ItemStack stack;
        if (source instanceof ItemStack itemStack) {
            if (itemStack.getItem() == Items.AIR) {
                return;
            }
            stack = itemStack;
        } else if (source instanceof FabricItemType itemType) {
            stack = itemType.toStack();
        } else {
            return;
        }

        Number requestedLine = lineNumber == null ? null : lineNumber.getSingle(event);
        if (lineNumber != null && requestedLine == null) {
            return;
        }

        List<String> lore = new ArrayList<>(readLore(stack));
        String[] stringDelta = toStringDelta(delta);
        if (requestedLine == null) {
            switch (mode) {
                case SET -> lore = splitLines(stringDelta);
                case ADD -> lore.addAll(splitLines(stringDelta));
                case DELETE -> lore.clear();
                case REMOVE -> {
                    if (stringDelta.length == 0 || lore.isEmpty()) {
                        break;
                    }
                    String updated = StringUtils.replaceFirst(String.join("\n", lore), stringDelta[0], "", true);
                    lore = updated.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(updated.split("\n", -1)));
                }
                default -> {
                }
            }
        } else {
            int lineIndex = Math.max(0, requestedLine.intValue() - 1);
            while (lore.size() <= lineIndex) {
                lore.add("");
            }
            switch (mode) {
                case SET -> lore.set(lineIndex, stringDelta.length == 0 ? "" : stringDelta[0]);
                case ADD -> lore.set(lineIndex, lore.get(lineIndex) + (stringDelta.length == 0 ? "" : stringDelta[0]));
                case DELETE -> lore.remove(lineIndex);
                case REMOVE -> {
                    if (stringDelta.length == 0) {
                        break;
                    }
                    lore.set(lineIndex, StringUtils.replaceFirst(lore.get(lineIndex), stringDelta[0], "", true));
                }
                default -> {
                }
            }
        }

        if (lore.isEmpty()) {
            stack.remove(DataComponents.LORE);
        } else {
            List<Component> components = new ArrayList<>(lore.size());
            for (String line : lore) {
                components.add(Component.literal(line));
            }
            stack.set(DataComponents.LORE, new ItemLore(components));
        }

        if (source instanceof FabricItemType itemType) {
            itemType.applyPrototype(stack);
        }
    }

    private static List<String> readLore(@Nullable Object source) {
        if (source == null) {
            return List.of();
        }
        ItemStack stack;
        if (source instanceof ItemStack itemStack) {
            stack = itemStack;
        } else if (source instanceof FabricItemType itemType) {
            stack = itemType.toStack();
        } else {
            return List.of();
        }
        if (stack.isEmpty()) {
            return List.of();
        }
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return List.of();
        }
        List<String> lines = new ArrayList<>(lore.lines().size());
        for (Component line : lore.lines()) {
            lines.add(line.getString());
        }
        return lines;
    }

    private static List<String> splitLines(String[] values) {
        List<String> lines = new ArrayList<>();
        for (String value : values) {
            lines.addAll(Arrays.asList(value.split("\n", -1)));
        }
        return lines;
    }

    private static String[] toStringDelta(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0) {
            return new String[0];
        }
        String[] converted = new String[delta.length];
        for (int i = 0; i < delta.length; i++) {
            converted[i] = delta[i] == null ? "" : delta[i].toString();
        }
        return converted;
    }

    @Override
    public boolean isSingle() {
        return lineNumber != null;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (lineNumber == null ? "lore of " : "line " + lineNumber.toString(event, debug) + " of lore of ")
                + item.toString(event, debug);
    }
}
