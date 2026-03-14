package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Player List Header and Footer")
@Description("The header and footer of the player list for a player. Can be changed and reset.")
@Example("set the header of the player's player list to \"Welcome!\"")
@Example("reset the footer of the player's tab list")
@Since("2.4")
public class ExprPlayerlistHeaderFooter extends SimplePropertyExpression<ServerPlayer, String> {

    static {
        register(ExprPlayerlistHeaderFooter.class, String.class, "(player|tab)[ ]list (0¦header|1¦footer) [(text|message)]", "players");
    }

    private boolean isHeader;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        super.init(exprs, matchedPattern, isDelayed, parseResult);
        isHeader = parseResult.mark == 0;
        return true;
    }

    @Override
    public String convert(ServerPlayer player) {
        String value = isHeader
            ? TabListExpressionSupport.getHeader(player)
            : TabListExpressionSupport.getFooter(player);
        return value != null ? value : "";
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET -> new Class[]{String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        String value = mode == ChangeMode.SET && delta != null && delta.length > 0 ? (String) delta[0] : null;
        for (ServerPlayer player : getExpr().getArray(event)) {
            if (isHeader) {
                TabListExpressionSupport.setHeader(player, value);
            } else {
                TabListExpressionSupport.setFooter(player, value);
            }
            String header = TabListExpressionSupport.getHeader(player);
            String footer = TabListExpressionSupport.getFooter(player);
            player.connection.send(new ClientboundTabListPacket(
                Component.literal(header != null ? header : ""),
                Component.literal(footer != null ? footer : "")
            ));
        }
    }

    @Override
    protected String getPropertyName() {
        return isHeader ? "tab list header" : "tab list footer";
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }
}
