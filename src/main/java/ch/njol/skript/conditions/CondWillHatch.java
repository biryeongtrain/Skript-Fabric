package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEggThrowEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Egg Will Hatch")
@Description("Whether the egg will hatch in a Player Egg Throw event.")
@Example("""
	on player egg throw:
		if an entity won't hatch:
			send "Better luck next time!" to the player
	""")
@Events("Egg Throw")
@Since("2.7")
public class CondWillHatch extends Condition {

	private static final @Nullable Class<?> EGG_THROW_EVENT = resolveEggThrowEventClass();

	static {
		Skript.registerCondition(CondWillHatch.class,
				"[the] egg (:will|will not|won't) hatch"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (EGG_THROW_EVENT == null || !getParser().isCurrentEvent(EGG_THROW_EVENT)) {
			Skript.error("You can't use the 'egg will hatch' condition outside of a Player Egg Throw event.");
			return false;
		}
		setNegated(!parseResult.hasTag("will"));
		return true;
	}

	@Override
	public boolean check(SkriptEvent event) {
		return event.handle() instanceof FabricEggThrowEventHandle eggThrow
			&& (eggThrow.hatching() ^ isNegated());
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "the egg " + (isNegated() ? "will not" : "will") + " hatch";
	}

	private static @Nullable Class<?> resolveEggThrowEventClass() {
		try {
			return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerEggThrow");
		} catch (ClassNotFoundException ignored) {
			return null;
		}
	}

}
