package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.MinecraftTag;
import org.skriptlang.skript.bukkit.tags.TagSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTag extends SimpleExpression<MinecraftTag> {

    private Expression<String> names;
    private MinecraftTag.Target target;
    private String defaultNamespace;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(String.class)) {
            return false;
        }
        names = (Expression<String>) expressions[0];
        target = switch (matchedPattern % 4) {
            case 1 -> MinecraftTag.Target.ITEM;
            case 2 -> MinecraftTag.Target.BLOCK;
            case 3 -> MinecraftTag.Target.ENTITY;
            default -> MinecraftTag.Target.ANY;
        };
        defaultNamespace = switch (matchedPattern / 4) {
            case 1 -> "minecraft";
            case 2 -> "minecraft";
            case 3 -> "skript";
            default -> "minecraft";
        };
        return true;
    }

    @Override
    protected MinecraftTag @Nullable [] get(SkriptEvent event) {
        List<MinecraftTag> tags = new ArrayList<>();
        for (String raw : names.getAll(event)) {
            ResourceLocation id = TagSupport.parseTagId(raw, defaultNamespace);
            if (id != null) {
                tags.add(new MinecraftTag(id, target));
            }
        }
        return tags.toArray(MinecraftTag[]::new);
    }

    @Override
    public boolean isSingle() {
        return names.isSingle();
    }

    @Override
    public Class<? extends MinecraftTag> getReturnType() {
        return MinecraftTag.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "tag " + names.toString(event, debug);
    }
}
