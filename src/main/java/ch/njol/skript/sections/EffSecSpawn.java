package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.List;

@Name("Spawn")
@Description({
	"Spawns entities. This can be used as an effect and as a section.",
	"",
	"If it is used as a section, the section is run before the entity is added to the world.",
	"You can modify the entity in this section, using for example 'event-entity' or 'cow'. ",
	"Do note that other event values, such as 'player', won't work in this section."
})
@Example("spawn 3 creepers at the targeted block")
@Example("spawn a ghast 5 meters above the player")
@Example("""
	spawn a zombie at the player:
		set name of the zombie to ""
	""")
@Since("1.0, 2.6.1 (with section)")
public class EffSecSpawn extends EffectSection {

	public static void register() {
		Skript.registerSection(EffSecSpawn.class,
				"(spawn|summon) %entitytypes% [%directions% %locations%]",
				"(spawn|summon) %number% of %entitytypes% [%directions% %locations%]"
		);
	}

	private Expression<FabricLocation> locations;
	private Expression<?> types;
	@Nullable
	private Expression<Number> amount;
	@Nullable
	public static Entity lastSpawned;
	@Nullable
	private Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
			@Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {

		amount = matchedPattern == 0 ? null : (Expression<Number>) (exprs[0]);
		types = exprs[matchedPattern];
		locations = Direction.combine(
				(Expression<? extends Direction>) exprs[1 + matchedPattern],
				(Expression<? extends FabricLocation>) exprs[2 + matchedPattern]
		);

		if (sectionNode != null) {
			trigger = SectionUtils.loadLinkedCode("spawn", (beforeLoading, afterLoading)
					-> loadCode(sectionNode, "spawn", beforeLoading, afterLoading));
			return trigger != null;
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(SkriptEvent event) {
		lastSpawned = null;

		Number numberAmount = amount != null ? amount.getSingle(event) : 1;
		if (numberAmount != null) {
			double amt = numberAmount.doubleValue();
			Object[] typeArray = this.types.getArray(event);
			for (FabricLocation location : locations.getArray(event)) {
				for (Object type : typeArray) {
					if (type instanceof EntityType entityType) {
						double typeAmount = amt * entityType.amount;
						for (int i = 0; i < typeAmount; i++) {
							if (trigger != null) {
								entityType.data.spawn(location, entity -> {
									lastSpawned = entity;
									SkriptEvent sectionEvent = new SkriptEvent(entity, event.server(), event.level(), event.player());
									Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
								});
							} else {
								lastSpawned = entityType.data.spawn(location);
							}
						}
					}
				}
			}
		}

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "spawn " + (amount != null ? amount.toString(event, debug) + " of " : "") +
				types.toString(event, debug) + " " + locations.toString(event, debug);
	}

}
