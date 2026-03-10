package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Level")
@Description("The experience level of a player.")
@Example("reduce the victim's level by 1")
@Example("set the player's level to 0")
@Example("""
	on level change:
		set {_diff} to future xp level - past exp level
		broadcast "%player%'s level changed by %{_diff}%!"
	""")
@Since("unknown (before 2.1), 2.13.2 (allow player default)")
@Events("level change")
public class ExprLevel extends SimplePropertyExpression<ServerPlayer, Long> {

    static {
        registerDefault(ExprLevel.class, Long.class, "[xp|exp[erience]] level", "players");
    }

    @Override
    protected Long[] get(SkriptEvent event, ServerPlayer[] source) {
        return get(source, player -> (long) player.experienceLevel);
    }

    @Override
    public @Nullable Long convert(ServerPlayer player) {
        return (long) player.experienceLevel;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.DELETE || mode == ChangeMode.RESET || mode == ChangeMode.SET
                || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE
                ? new Class[]{Number.class}
                : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = delta == null ? 0 : ((Number) delta[0]).intValue();
        for (ServerPlayer player : getExpr().getArray(event)) {
            int level = player.experienceLevel;
            switch (mode) {
                case SET -> level = amount;
                case ADD -> level += amount;
                case REMOVE -> level -= amount;
                case DELETE, RESET -> level = 0;
                default -> {
                }
            }
            player.experienceLevel = Math.max(0, level);
        }
    }

    @Override
    public Class<Long> getReturnType() {
        return Long.class;
    }
    @Override
    protected String getPropertyName() {
        return "level";
    }
}
