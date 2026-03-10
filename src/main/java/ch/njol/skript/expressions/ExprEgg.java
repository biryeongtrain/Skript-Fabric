package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("The Egg")
@Description("The egg thrown in a Player Egg Throw event.")
@Example("spawn an egg at the egg")
@Events("Egg Throw")
@Since("2.7")
public class ExprEgg extends EventValueExpression<ThrownEgg> {

    static {
        register(ExprEgg.class, ThrownEgg.class, "[thrown] egg");
    }

    public ExprEgg() {
        super(ThrownEgg.class, true);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the egg";
    }
}
