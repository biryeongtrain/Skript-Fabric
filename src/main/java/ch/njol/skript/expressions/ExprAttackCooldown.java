package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@Name("Attack Cooldown")
@Description({
        "Returns the current cooldown for a player's attack. This is used to calculate damage, with 1.0 representing a fully charged attack and 0.0 representing a non-charged attack.",
        "NOTE: Currently this can not be set to anything."
})
@Example("""
    on damage:
        if attack cooldown of attacker < 1:
            set damage to 0
            send "Your hit was too weak! wait until your weapon is fully charged next time." to attacker
    """)
@Since("2.6.1")
@RequiredPlugins("Minecraft 1.15+")
public class ExprAttackCooldown extends SimplePropertyExpression<ServerPlayer, Float> {

    static {
        register(ExprAttackCooldown.class, Float.class, "attack cooldown", "players");
    }

    @Override
    @Nullable
    public Float convert(ServerPlayer player) {
        return player.getAttackStrengthScale(0.0F);
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    protected String getPropertyName() {
        return "attack cooldown";
    }
}
