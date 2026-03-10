package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.MoonPhase;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class ExprMoonPhase extends SimplePropertyExpression<ServerLevel, MoonPhase> {

    static {
        register(ExprMoonPhase.class, MoonPhase.class, "(lunar|moon) phase[s]", "worlds");
    }

    @Override
    public @Nullable MoonPhase convert(ServerLevel world) {
        return MoonPhase.of(world);
    }

    @Override
    public Class<? extends MoonPhase> getReturnType() {
        return MoonPhase.class;
    }

    @Override
    protected String getPropertyName() {
        return "moon phase";
    }
}
