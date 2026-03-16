package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Mending Repair Amount")
@Description({"The number of durability points an item is to be repaired in a mending event.",
		"Modifying the repair amount will affect how much experience is given to the player after mending."})
@Example("""
	on item mend:
		set the mending repair amount to 100
	""")
@Since("2.5.1, Fabric")
public class ExprMendingRepairAmount extends SimpleExpression<Long> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprMendingRepairAmount.class, Long.class, "[the] [mending] repair amount");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<?>[] supportedEvents() {
		return new Class<?>[]{FabricEventCompatHandles.Mending.class};
	}

	@Override
	protected Long @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricEventCompatHandles.Mending handle)) {
			return null;
		}
		return new Long[]{(long) handle.repairAmount()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event.handle() instanceof FabricEventCompatHandles.Mending handle)) {
			return;
		}
		int value = delta == null ? 0 : Math.max(0, ((Number) delta[0]).intValue());
		int updated = switch (mode) {
			case SET -> value;
			case ADD -> handle.repairAmount() + value;
			case REMOVE -> Math.max(0, handle.repairAmount() - value);
			case DELETE -> 0;
			default -> handle.repairAmount();
		};
		handle.setRepairAmount(updated);
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "the mending repair amount";
	}
}
