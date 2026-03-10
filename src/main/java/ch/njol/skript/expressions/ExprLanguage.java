package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@Name("Language")
@Description("The language setting currently reported by a player's client.")
@Example("send language of player")
@Since("2.3, Fabric")
public class ExprLanguage extends SimplePropertyExpression<ServerPlayer, String> {

    static {
        register(ExprLanguage.class, String.class, "[([currently] selected|current)] [game] (language|locale) [setting]", "players");
    }

    @Override
    public @Nullable String convert(ServerPlayer player) {
        return player.clientInformation().language();
    }

    @Override
    protected String getPropertyName() {
        return "language";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}
