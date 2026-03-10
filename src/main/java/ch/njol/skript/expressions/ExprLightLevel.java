package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Light Level")
@Description("Gets the light level at a certain location which ranges from 0 to 15.")
@Example("light level of player's location")
@Since("1.3.4")
public class ExprLightLevel extends PropertyExpression<FabricLocation, Byte> {

    private static final int SKY = 1;
    private static final int BLOCK = 2;
    private static final int ANY = SKY | BLOCK;

    static {
        Skript.registerExpression(ExprLightLevel.class, Byte.class,
                "[(1¦sky|1¦sun|2¦block)[ ]]light[ ]level [of %location%]");
    }

    private int whatLight = ANY;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends FabricLocation>) exprs[0]);
        whatLight = parseResult.mark == 0 ? ANY : parseResult.mark;
        return true;
    }

    @Override
    public Class<Byte> getReturnType() {
        return Byte.class;
    }

    @Override
    protected Byte[] get(SkriptEvent event, FabricLocation[] source) {
        return get(source, location -> {
            if (location.level() == null) {
                return null;
            }
            BlockPos pos = BlockPos.containing(location.position());
            int level = switch (whatLight) {
                case BLOCK -> location.level().getBrightness(LightLayer.BLOCK, pos);
                case SKY -> location.level().getBrightness(LightLayer.SKY, pos);
                default -> location.level().getMaxLocalRawBrightness(pos);
            };
            return (byte) level;
        });
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (whatLight == BLOCK ? "block " : whatLight == SKY ? "sky " : "") + "light level of "
                + getExpr().toString(event, debug);
    }
}
