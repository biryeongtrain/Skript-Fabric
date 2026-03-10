package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Inventory Close Reason")
@Description("The inventory close reason of an inventory-close style event handle.")
@Example("inventory close reason is teleport")
@Events("Inventory Close")
@Since("2.8.0")
public final class ExprInventoryCloseReason extends EventValueExpression<Object> {

    static {
        Skript.registerExpression(ExprInventoryCloseReason.class, Object.class, "[the] inventory clos(e|ing) (reason|cause)");
    }

    public ExprInventoryCloseReason() {
        super(Object.class);
    }

    @Override
    public boolean init() {
        if (!ReflectiveHandleAccess.currentEventSupports("reason", "getReason", "cause", "getCause")) {
            Skript.error("The 'inventory close reason' expression can only be used in an inventory close event");
            return false;
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object value = ReflectiveHandleAccess.invokeNoArg(event.handle(), "reason", "getReason", "cause", "getCause");
        return value == null ? null : new Object[]{value};
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "inventory close reason";
    }
}
