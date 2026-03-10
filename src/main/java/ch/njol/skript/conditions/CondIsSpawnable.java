package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsSpawnable extends Condition {

    static {
        Skript.registerCondition(
                CondIsSpawnable.class,
                "%entitydatas% is spawnable [in [the [world]] %world%]",
                "%entitydatas% can be spawned [in [the [world]] %world%]",
                "%entitydatas% (isn't|is not) spawnable [in [the [world]] %world%]",
                "%entitydatas% (can't|can not) be spawned [in [the [world]] %world%]"
        );
    }

    private Expression<EntityData<?>> datas;
    private Expression<ServerLevel> world;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        datas = (Expression<EntityData<?>>) exprs[0];
        world = (Expression<ServerLevel>) exprs[1];
        setNegated(matchedPattern >= 2);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (world != null) {
            world.getSingle(event);
        }
        return datas.check(event, ConditionRuntimeSupport::isSpawnable, isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(datas, "is");
        if (isNegated()) {
            builder.append("not");
        }
        builder.append("spawnable");
        if (world != null) {
            builder.append("in", world);
        }
        return builder.toString();
    }
}
