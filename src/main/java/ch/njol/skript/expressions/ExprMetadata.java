package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.fabric.compat.FabricMetadataStore;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operation;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Metadata")
@Description("Metadata is a way to store temporary data on entities that disappears after a server restart.")
@Example("set metadata value \"healer\" of player to true")
@Example("broadcast \"%metadata value \"healer\" of player%\"")
@Example("clear metadata value \"healer\" of player")
@Since("2.2-dev36, 2.10 (add, remove)")
public class ExprMetadata<T> extends SimpleExpression<T> {

	static {
		//noinspection unchecked
		Skript.registerExpression(ExprMetadata.class, Object.class,
				"metadata [(value|tag)[s]] %strings% of %entities%",
				"%entities%'[s] metadata [(value|tag)[s]] %strings%"
		);
	}

	private final ExprMetadata<?> source;
	private final Class<? extends T>[] types;
	private final Class<T> superType;

	private @UnknownNullability Expression<String> keys;
	private @UnknownNullability Expression<Entity> holders;

	public ExprMetadata() {
		//noinspection unchecked
		this(null, (Class<? extends T>) Object.class);
	}

	@SafeVarargs
	private ExprMetadata(ExprMetadata<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.keys = source.keys;
			this.holders = source.holders;
		}
		this.types = types;
		//noinspection unchecked
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		holders = (Expression<Entity>) exprs[matchedPattern ^ 1];
		keys = (Expression<String>) exprs[matchedPattern];
		return true;
	}

	@Override
	protected T @Nullable [] get(SkriptEvent event) {
		List<Object> values = new ArrayList<>();
		String[] keys = this.keys.getArray(event);
		for (Entity holder : holders.getArray(event)) {
			for (String key : keys) {
				Object value = FabricMetadataStore.getMetadata(holder, key);
				if (value != null)
					values.add(value);
			}
		}
		try {
			return Converters.convert(values.toArray(), types, superType);
		} catch (ClassCastException exception) {
			//noinspection unchecked
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE -> new Class[]{Object.class};
			default -> null;
		};
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		String[] keys = this.keys.getArray(event);
		for (Entity holder : holders.getArray(event)) {
			for (String key : keys) {
				switch (mode) {
					case SET -> FabricMetadataStore.setMetadata(holder, key, delta[0]);
					case ADD, REMOVE -> {
						assert delta != null;
						Operator operator = mode == ChangeMode.ADD ? Operator.ADDITION : Operator.SUBTRACTION;
						Object value = FabricMetadataStore.getMetadata(holder, key);
						OperationInfo<?, ?, ?> info;
						if (value != null) {
							info = Arithmetics.getOperationInfo(operator, value.getClass(), delta[0].getClass());
							if (info == null)
								continue;
						} else {
							info = Arithmetics.getOperationInfo(operator, delta[0].getClass(), delta[0].getClass());
							if (info == null)
								continue;
							value = Arithmetics.getDefaultValue(info.left());
							if (value == null)
								continue;
						}
						Object newValue = ((Operation) info.operation()).calculate(value, delta[0]);
						FabricMetadataStore.setMetadata(holder, key, newValue);
					}
					case DELETE -> FabricMetadataStore.removeMetadata(holder, key);
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return holders.isSingle() && keys.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	public Class<? extends T>[] possibleReturnTypes() {
		return Arrays.copyOf(types, types.length);
	}

	@Override
	@SafeVarargs
	public final <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprMetadata<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "metadata values " + keys.toString(event, debug) + " of " + holders.toString(event, debug);
	}

}
