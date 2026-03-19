package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Color Items")
@Description("Colors items in a given color.")
@Example("dye player's helmet blue")
@Example("color the player's tool red")
@Since("2.0, 2.2-dev26 (maps and potions)")
public class EffColorItems extends Effect {

    private static boolean registered;

    private Expression<FabricItemType> items;
    private Expression<Color> color;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffColorItems.class,
                "(dye|colo[u]r|paint) %itemtypes% %color%",
                "(dye|colo[u]r|paint) %itemtypes% \\(%number%, %number%, %number%\\)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        items = (Expression<FabricItemType>) exprs[0];
        if (matchedPattern == 0) {
            color = (Expression<Color>) exprs[1];
            return true;
        }
        Expression<Number> red = (Expression<Number>) exprs[1];
        Expression<Number> green = (Expression<Number>) exprs[2];
        Expression<Number> blue = (Expression<Number>) exprs[3];
        color = new SimpleExpression<>() {
            @Override
            protected Color @Nullable [] get(SkriptEvent event) {
                Number r = red.getSingle(event);
                Number g = green.getSingle(event);
                Number b = blue.getSingle(event);
                if (r == null || g == null || b == null) {
                    return null;
                }
                Color value = new Color() {
                    @Override
                    public int red() {
                        return r.intValue();
                    }

                    @Override
                    public int green() {
                        return g.intValue();
                    }

                    @Override
                    public int blue() {
                        return b.intValue();
                    }

                    @Override
                    public String toString() {
                        return "(" + red() + ", " + green() + ", " + blue() + ")";
                    }
                };
                return new Color[]{value};
            }

            @Override
            public boolean isSingle() {
                return true;
            }

            @Override
            public Class<? extends Color> getReturnType() {
                return Color.class;
            }

            @Override
            public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
                return true;
            }

            @Override
            public String toString(@Nullable SkriptEvent event, boolean debug) {
                return "(" + red.toString(event, debug) + ", " + green.toString(event, debug) + ", "
                        + blue.toString(event, debug) + ")";
            }
        };
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Color c = color.getSingle(event);
        if (c == null) return;
        int rgb = (c.red() << 16) | (c.green() << 8) | c.blue();
        for (FabricItemType itemType : items.getArray(event)) {
            ItemStack stack = itemType.toStack();
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
            itemType.applyPrototype(stack);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "dye " + items.toString(event, debug) + " " + color.toString(event, debug);
    }
}
