package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffSetTestItemName extends Effect {

    private Expression<ItemStack> itemExpression;
    private Expression<String> nameExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        itemExpression = (Expression<ItemStack>) expressions[0];
        nameExpression = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        ItemStack item = itemExpression.getSingle(event);
        String name = nameExpression.getSingle(event);
        if (item == null || name == null) {
            throw new IllegalStateException("set test item name effect received incomplete item or name.");
        }

        String normalized = normalizeName(name);
        if (normalized.isBlank()) {
            item.remove(DataComponents.CUSTOM_NAME);
            return;
        }
        item.set(DataComponents.CUSTOM_NAME, Component.literal(normalized));
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
        return "set test item name";
    }
}
