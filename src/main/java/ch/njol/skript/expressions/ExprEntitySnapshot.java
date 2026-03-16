package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("Entity Snapshot")
@Description({
	"Returns the entity snapshot of a provided entity as a string representation.",
	"This captures the entity type and UUID at the time this expression is used."
})
@Example("""
	set {_snapshot} to the entity snapshot of event-entity
	""")
@Since("2.10, Fabric")
public class ExprEntitySnapshot extends SimplePropertyExpression<Entity, String> {

	static {
		register(ExprEntitySnapshot.class, String.class, "entity snapshot", "entities");
	}

	@Override
	public @Nullable String convert(Entity entity) {
		String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
		return type + ":" + entity.getStringUUID();
	}

	@Override
	protected String getPropertyName() {
		return "entity snapshot";
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
}
