package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.List;

@Name("Create WorldBorder")
@Description({
	"Creates a new, unused world border. World borders can be assigned to either worlds or specific players.",
	"Borders assigned to worlds apply to all players in that world.",
	"Borders assigned to players apply only to those players, and different players can have different borders."
})
@Example("""
	on join:
		set {_location} to location of player
		set worldborder of player to a virtual worldborder:
			set worldborder radius to 25
			set world border center of event-worldborder to {_location}
	""")
@Example("""
	on load:
		set worldborder of world "world" to a worldborder:
			set worldborder radius of event-worldborder to 200
			set worldborder center of event-worldborder to location(0, 64, 0)
			set worldborder warning distance of event-worldborder to 5
	""")
@Since("2.11")
public class ExprSecCreateWorldBorder extends SectionExpression<WorldBorder> {

	public static void register() {
		Skript.registerExpression(ExprSecCreateWorldBorder.class, WorldBorder.class, "a [virtual] world[ ]border");
	}

	private @Nullable Trigger trigger;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			trigger = SectionUtils.loadLinkedCode("create worldborder", (beforeLoading, afterLoading)
					-> loadCode(node, "create worldborder", beforeLoading, afterLoading));
			return trigger != null;
		}
		return true;
	}

	@Override
	protected WorldBorder @Nullable [] get(SkriptEvent event) {
		WorldBorder worldBorder = new WorldBorder();
		if (trigger == null)
			return new WorldBorder[]{worldBorder};
		SkriptEvent sectionEvent = new SkriptEvent(worldBorder, event.server(), event.level(), event.player());
		Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		return new WorldBorder[]{worldBorder};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<WorldBorder> getReturnType() {
		return WorldBorder.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "a virtual worldborder";
	}

}
