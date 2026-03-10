package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLightLevel extends PropertyExpression<FabricLocation, Byte> {

    static {
        Skript.registerExpression(ExprLightLevel.class, Byte.class,
                "[(1¦sky|1¦sun|2¦block)[ ]]light[ ]level [(of|%direction%) %location%]");
    }

    private static final int SKY = 1;
    private static final int BLOCK = 2;
    private static final int ANY = SKY | BLOCK;

    private int lightType = ANY;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr(Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends FabricLocation>) exprs[1]));
        lightType = parseResult.mark == 0 ? ANY : parseResult.mark;
        return true;
    }

    @Override
    protected Byte[] get(SkriptEvent event, FabricLocation[] source) {
        return get(source, location -> {
            if (location.level() == null) {
                return null;
            }
            var pos = net.minecraft.core.BlockPos.containing(location.position());
            int block = location.level().getBrightness(LightLayer.BLOCK, pos);
            int sky = location.level().getBrightness(LightLayer.SKY, pos);
            int value = switch (lightType) {
                case BLOCK -> block;
                case SKY -> sky;
                default -> Math.max(block, sky);
            };
            return (byte) value;
        });
    }

    @Override
    public Class<Byte> getReturnType() {
        return Byte.class;
    }
}
