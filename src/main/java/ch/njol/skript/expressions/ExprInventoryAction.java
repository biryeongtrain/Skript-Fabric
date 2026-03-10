package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Inventory Action")
@Description("The inventory action of an inventory event handle.")
@Example("inventory action is pickup all")
@Since("2.2-dev16")
public final class ExprInventoryAction extends EventValueExpression<Object> {

    static {
        register(ExprInventoryAction.class, Object.class, "inventory action");
    }

    public ExprInventoryAction() {
        super(Object.class);
    }

    @Override
    public boolean init() {
        if (!ReflectiveHandleAccess.currentEventSupports("action", "getAction")) {
            Skript.error("The 'inventory action' expression may only be used in an inventory event.");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object value = ReflectiveHandleAccess.invokeNoArg(event.handle(), "action", "getAction");
        return value == null ? null : new Object[]{value};
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "inventory action";
    }
}
