package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.MinecraftTag;
import org.skriptlang.skript.bukkit.tags.TagSupport;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffRegisterTag extends Effect {

    private static final Pattern KEY_PATTERN = Pattern.compile("[a-zA-Z0-9/._:-]+");

    private Expression<String> name;
    private Expression<?> contents;
    private MinecraftTag.Target target;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        name = (Expression<String>) expressions[0];
        contents = expressions[1];
        target = switch (matchedPattern) {
            case 0, 1 -> MinecraftTag.Target.ITEM;
            case 2, 3 -> MinecraftTag.Target.BLOCK;
            case 4, 5 -> MinecraftTag.Target.ENTITY;
            default -> MinecraftTag.Target.ANY;
        };
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        String rawName = name.getSingle(event);
        if (rawName == null) {
            return;
        }
        String normalized = MinecraftResourceParser.normalize(rawName);
        if (normalized.startsWith("skript:")) {
            normalized = normalized.substring(7);
        }
        if (!KEY_PATTERN.matcher(normalized).matches()) {
            return;
        }
        Identifier id = TagSupport.parseTagId(normalized, "skript");
        if (id == null) {
            return;
        }
        TagSupport.registerCustomTag(target, id, contents.getAll(event));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "register tag named " + name.toString(event, debug);
    }
}
