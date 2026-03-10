package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.fabric.placeholder.SkriptTextPlaceholders;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffSetTestEntityName extends Effect {

    private Expression<Entity> entityExpression;
    private Expression<String> nameExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        entityExpression = (Expression<Entity>) expressions[0];
        nameExpression = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Entity[] entities = entityExpression.getArray(event);
        String name = nameExpression.getSingle(event);
        if (entities.length == 0 || name == null) {
            throw new IllegalStateException("set test entity name effect received incomplete entity or name.");
        }
        Component resolved = SkriptTextPlaceholders.resolveComponent(normalizeName(name), event);
        for (Entity entity : entities) {
            entity.setCustomName(resolved);
        }
    }

    private String normalizeName(String name) {
        String normalized = name.trim();
        while (normalized.length() >= 2) {
            if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                    || (normalized.startsWith("'") && normalized.endsWith("'"))) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
                continue;
            }
            if ((normalized.startsWith("\\\"") && normalized.endsWith("\\\""))
                    || (normalized.startsWith("\\'") && normalized.endsWith("\\'"))) {
                normalized = normalized.substring(2, normalized.length() - 2).trim();
                continue;
            }
            break;
        }
        return normalized.replace("\\\"", "").replace("\\'", "");
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return "set test entity name";
    }
}
