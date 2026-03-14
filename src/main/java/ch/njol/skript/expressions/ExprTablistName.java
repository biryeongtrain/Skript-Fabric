package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Player Tab List Name")
@Description("The name of a player as shown in the tab list. Can be changed to a custom name, or reset to the default.")
@Example("set tab list name of player to \"&6%player%\"")
@Since("2.0")
public class ExprTablistName extends SimplePropertyExpression<ServerPlayer, String> {

    static {
        register(ExprTablistName.class, String.class, "(player|tab)[ ]list name[s]", "players");
    }

    @Override
    public String convert(ServerPlayer player) {
        String custom = TabListExpressionSupport.getDisplayName(player);
        return custom != null ? custom : player.getGameProfile().getName();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET -> new Class[]{String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        String name = mode == ChangeMode.SET && delta != null && delta.length > 0 ? (String) delta[0] : null;
        for (ServerPlayer player : getExpr().getArray(event)) {
            TabListExpressionSupport.setDisplayName(player, name);
            player.connection.send(
                ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(java.util.List.of(player))
            );
        }
    }

    @Override
    protected String getPropertyName() {
        return "tab list name";
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }
}
