package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Enchant/Disenchant")
@Description("Enchant or disenchant an existing item.")
@Example("enchant the player's tool with sharpness 5")
@Example("enchant the player's tool at level 30")
@Example("disenchant the player's tool")
@Since("2.0, 2.13 (at level)")
public class EffEnchant extends Effect {

    private enum Operation {
        ENCHANT,
        ENCHANT_AT_LEVEL,
        DISENCHANT
    }

    private static boolean registered;
    private static final Patterns<Operation> PATTERNS = new Patterns<>(new Object[][]{
            {"enchant %~itemtypes% with %enchantmenttypes%", Operation.ENCHANT},
            {"[naturally|randomly] enchant %~itemtypes% at level %number%[treasure:[,] allowing treasure enchant[ment]s]",
                    Operation.ENCHANT_AT_LEVEL},
            {"disenchant %~itemtypes%", Operation.DISENCHANT}
    });

    private Expression<FabricItemType> items;
    private @Nullable Expression<?> enchantments;
    private @Nullable Expression<Number> level;
    private boolean treasure;
    private Operation operation;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffEnchant.class, PATTERNS.getPatterns());
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = (Expression<FabricItemType>) exprs[0];
        if (matchedPattern == 0) {
            enchantments = exprs[1];
        } else if (matchedPattern == 1) {
            level = (Expression<Number>) exprs[1];
            treasure = parseResult.hasTag("treasure");
        }
        operation = PATTERNS.getInfo(matchedPattern);
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (operation) {
            case ENCHANT -> "enchant " + items.toString(event, debug) + " with "
                    + (enchantments == null ? "" : enchantments.toString(event, debug));
            case ENCHANT_AT_LEVEL -> "enchant " + items.toString(event, debug) + " at level "
                    + (level == null ? "" : level.toString(event, debug))
                    + (treasure ? " allowing treasure enchantments" : "");
            case DISENCHANT -> "disenchant " + items.toString(event, debug);
        };
    }
}
