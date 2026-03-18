package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Experience")
@Description({"How much experience was spawned in an experience spawn event.",
        "Can be changed with set, add, or remove."})
@Example("""
	on experience spawn:
		send "%spawned experience%" to player
	""")
@Since("2.1")
@Events("experience spawn")
public class ExprExperience extends SimpleExpression<Experience> {

    static {
        Skript.registerExpression(ExprExperience.class, Experience.class, "[the] (spawned|dropped|) [e]xp[erience] [orb[s]]");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricEventCompatHandles.ExperienceSpawn.class)) {
            Skript.error("The 'experience' expression can only be used in experience spawn events");
            return false;
        }
        return true;
    }

    @Override
    protected Experience @Nullable [] get(SkriptEvent event) {
        if (event.handle() instanceof FabricEventCompatHandles.ExperienceSpawn handle) {
            return new Experience[]{new Experience(handle.amount())};
        }
        return new Experience[0];
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Experience.class, Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricEventCompatHandles.ExperienceSpawn handle)) return;
        if (delta == null || delta.length == 0) return;
        int value;
        if (delta[0] instanceof Experience exp) {
            value = exp.getXP();
        } else if (delta[0] instanceof Number number) {
            value = number.intValue();
        } else {
            return;
        }
        int current = handle.amount();
        switch (mode) {
            case SET -> handle.setAmount(Math.max(0, value));
            case ADD -> handle.setAmount(Math.max(0, current + value));
            case REMOVE -> handle.setAmount(Math.max(0, current - value));
            default -> {}
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Experience> getReturnType() {
        return Experience.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the experience";
    }
}
