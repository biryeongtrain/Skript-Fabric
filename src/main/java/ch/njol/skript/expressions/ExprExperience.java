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
@Description("How much experience was spawned in an experience spawn event.")
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
        Skript.error("The experience value cannot currently be changed on this compatibility surface");
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
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
