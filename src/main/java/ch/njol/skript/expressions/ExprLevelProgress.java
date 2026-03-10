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

@Name("Level Progress")
@Description({
        "The player's progress in reaching the next level, this represents the experience bar in the game. Please note that this value is between 0 and 1 (e.g. 0.5 = half experience bar).",
        "Changing this value can cause the player's level to change if the resulting level progess is negative or larger than 1, e.g. <code>increase the player's level progress by 0.5</code> will make the player gain a level if their progress was more than 50%."
})
@Example("""
    # use the exp bar as mana
    on rightclick with a blaze rod:
        player's level progress is larger than 0.2
        shoot a fireball from the player
        reduce the player's level progress by 0.2
    every 2 seconds:
        loop all players:
            level progress of loop-player is smaller than 0.9:
                increase level progress of the loop-player by 0.1
            else:
                set level progress of the loop-player to 0.99
    on xp spawn:
        cancel event
    """)
@Since("2.0")
@Events("level change")
public class ExprLevelProgress extends SimplePropertyExpression<ServerPlayer, Number> {

    static {
        register(ExprLevelProgress.class, Number.class, "level progress", "players");
    }

    @Override
    public Number convert(ServerPlayer player) {
        return player.experienceProgress;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        float amount = delta == null || delta.length == 0 ? 0 : ((Number) delta[0]).floatValue();
        for (ServerPlayer player : getExpr().getArray(event)) {
            float changed = switch (mode) {
                case SET -> amount;
                case ADD -> player.experienceProgress + amount;
                case REMOVE -> player.experienceProgress - amount;
                case DELETE, RESET -> 0;
            };
            player.experienceLevel = Math.max(0, player.experienceLevel + (int) Math.floor(changed));
            player.experienceProgress = Float.isFinite(changed) ? (float) (changed - Math.floor(changed)) : 0;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "level progress";
    }
}
