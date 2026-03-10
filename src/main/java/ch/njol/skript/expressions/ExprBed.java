package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public class ExprBed extends SimplePropertyExpression<GameProfile, FabricLocation> {

    static {
        register(ExprBed.class, FabricLocation.class, "bed[s]", "offlineplayers");
    }

    @Override
    public @Nullable FabricLocation convert(GameProfile profile) {
        return null;
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String getPropertyName() {
        return "bed";
    }
}
