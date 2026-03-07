package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.MinecraftTag;
import org.skriptlang.skript.bukkit.tags.TagSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTagsOfType extends SimpleExpression<MinecraftTag> {

    private MinecraftTag.Target target;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        target = switch (matchedPattern) {
            case 1 -> MinecraftTag.Target.ITEM;
            case 2 -> MinecraftTag.Target.BLOCK;
            case 3 -> MinecraftTag.Target.ENTITY;
            default -> MinecraftTag.Target.ANY;
        };
        return true;
    }

    @Override
    protected MinecraftTag @Nullable [] get(SkriptEvent event) {
        List<MinecraftTag> tags = TagSupport.allTags(target);
        return tags.toArray(MinecraftTag[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends MinecraftTag> getReturnType() {
        return MinecraftTag.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "all tags";
    }
}
