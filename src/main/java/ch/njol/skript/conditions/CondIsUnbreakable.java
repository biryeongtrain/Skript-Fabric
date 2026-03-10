package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Unbreakable")
@Description("Checks whether an item is unbreakable.")
@Example("""
    if event-item is unbreakable:
        send "This item is unbreakable!" to player
    """)
@Example("""
    if tool of {_p} is breakable:
        send "Your tool is breakable!" to {_p}
    """)
@Since("2.5.1, 2.9.0 (breakable)")
public class CondIsUnbreakable extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsUnbreakable.class, "[:un]breakable", "itemtypes");
    }

    private boolean breakable;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        breakable = !parseResult.hasTag("un");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public boolean check(FabricItemType item) {
        return (item.toStack().get(DataComponents.UNBREAKABLE) != null) ^ breakable;
    }

    @Override
    protected String getPropertyName() {
        return breakable ? "breakable" : "unbreakable";
    }
}
