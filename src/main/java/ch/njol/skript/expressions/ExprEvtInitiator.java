package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.Container;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.runtime.FabricInventoryMoveEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Initiator Inventory")
@Description("Returns the initiator inventory in an inventory item move event.")
@Example("""
	on inventory item move:
		broadcast "Item transport initiated!"
	""")
@Events("Inventory Item Move")
@Since("2.8.0, Fabric")
public class ExprEvtInitiator extends SimpleExpression<FabricInventory> {

	static {
		Skript.registerExpression(ExprEvtInitiator.class, FabricInventory.class, "[the] [event-]initiator[( |-)inventory]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(FabricInventoryMoveEventHandle.class)) {
			Skript.error("'event-initiator' can only be used in an 'inventory item move' event.");
			return false;
		}
		return true;
	}

	@Override
	protected FabricInventory @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricInventoryMoveEventHandle handle)) {
			return new FabricInventory[0];
		}
		Container initiator = handle.initiator();
		return new FabricInventory[]{new FabricInventory(initiator)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends FabricInventory> getReturnType() {
		return FabricInventory.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "event-initiator-inventory";
	}
}
