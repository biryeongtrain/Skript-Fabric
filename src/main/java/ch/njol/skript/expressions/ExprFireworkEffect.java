package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.component.FireworkExplosion;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricFireworkEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprFireworkEffect extends SimpleExpression<FabricFireworkEffect> {

    static {
        Skript.registerExpression(
                ExprFireworkEffect.class,
                FabricFireworkEffect.class,
                "(1¦|2¦flickering|3¦trailing|4¦flickering trailing|5¦trailing flickering) %fireworktype% [firework [effect]] colo[u]red %colors%",
                "(1¦|2¦flickering|3¦trailing|4¦flickering trailing|5¦trailing flickering) %fireworktype% [firework [effect]] colo[u]red %colors% fad(e|ing) [to] %colors%"
        );
    }

    private Expression<FireworkExplosion.Shape> type;
    private Expression<Color> color;
    private @Nullable Expression<Color> fade;
    private boolean flicker;
    private boolean trail;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        flicker = parseResult.mark == 2 || parseResult.mark > 3;
        trail = parseResult.mark >= 3;
        type = (Expression<FireworkExplosion.Shape>) exprs[0];
        color = (Expression<Color>) exprs[1];
        fade = matchedPattern == 1 ? (Expression<Color>) exprs[2] : null;
        return true;
    }

    @Override
    protected FabricFireworkEffect @Nullable [] get(SkriptEvent event) {
        FireworkExplosion.Shape shape = type.getSingle(event);
        if (shape == null) {
            return null;
        }
        Color[] colors = color.getArray(event);
        Color[] fadeColors = fade == null ? new Color[0] : fade.getArray(event);
        return new FabricFireworkEffect[]{new FabricFireworkEffect(shape, colors, fadeColors, flicker, trail)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricFireworkEffect> getReturnType() {
        return FabricFireworkEffect.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "firework effect " + type.toString(event, debug);
    }
}
