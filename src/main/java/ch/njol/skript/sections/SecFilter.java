package ch.njol.skript.sections;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Name("Filter")
@Description({
	"Filters a variable list based on the supplied conditions. Unlike the filter expression, this effect " +
	"maintains the indices of the filtered list.",
	"It also supports filtering based on meeting any of the given criteria, rather than all, like multi-line if statements."
})
@Example("set {_a::*} to integers between -10 and 10")
@Example("""
	filter {_a::*} to match:
		input is a number
		mod(input, 2) = 0
		input > 0
	send {_a::*} # sends 2, 4, 6, 8, and 10
	""")
@Since("2.10")
public class SecFilter extends Section implements InputSource {

	public static void register() {
		Skript.registerSection(SecFilter.class,
				"filter %~objects% to match [:any|all]");
		if (!ParserInstance.isRegistered(InputSource.InputData.class))
			ParserInstance.registerData(InputSource.InputData.class, InputSource.InputData::new);
	}

	private @UnknownNullability Variable<?> unfilteredObjects;
	private final List<Condition> conditions = new ArrayList<>();
	private boolean isAny;

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;
	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable)) {
			Skript.error("You can only filter list variables!");
			return false;
		}
		unfilteredObjects = (Variable<?>) expressions[0];
		isAny = parseResult.hasTag("any");

		ParserInstance parser = getParser();
		if (sectionNode.isEmpty()) {
			Skript.error("filter sections must contain at least one condition");
			return false;
		}
		InputSource.InputData inputData = getParser().getData(InputSource.InputData.class);
		InputSource originalSource = inputData.getSource();
		inputData.setSource(this);
		try {
			for (Node childNode : sectionNode) {
				if (!(childNode instanceof SimpleNode)) {
					Skript.error("Filter sections may not contain other sections");
					return false;
				}
				String childKey = childNode.getKey();
				if (childKey != null) {
					childKey = ScriptLoader.replaceOptions(childKey);
					parser.setNode(childNode);
					Condition condition = Condition.parse(childKey, "Can't understand this condition: '" + childKey + "'");
					parser.setNode(sectionNode);
					if (condition == null)
						return false;
					conditions.add(condition);
				}
			}
		} finally {
			inputData.setSource(originalSource);
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(SkriptEvent event) {
		String varName = unfilteredObjects.getName().toString(event);
		String varSubName = varName.substring(0, varName.length() - 1);
		boolean local = unfilteredObjects.isLocal();

		Map<String, Object> allEntries = Variables.getVariablesWithPrefix(varSubName, event, local);
		if (allEntries.isEmpty())
			return getNext();
		int initialSize = allEntries.size();

		List<Map.Entry<String, Object>> toKeep = new ArrayList<>();
		List<String> toRemove = new ArrayList<>();

		for (Map.Entry<String, Object> entry : allEntries.entrySet()) {
			String fullKey = entry.getKey();
			String index = fullKey.substring(varSubName.length());
			currentValue = entry.getValue();
			currentIndex = index;
			boolean matches = isAny
					? conditions.stream().anyMatch(c -> c.check(event))
					: conditions.stream().allMatch(c -> c.check(event));
			if (matches) {
				toKeep.add(entry);
			} else {
				toRemove.add(fullKey);
			}
		}

		// optimize by either removing or clearing + adding depending on which is fewer operations
		if (toKeep.size() < initialSize / 2) {
			Variables.setVariable(varName, null, event, local);
			for (Map.Entry<String, Object> entry : toKeep)
				Variables.setVariable(entry.getKey(), entry.getValue(), event, local);
		} else {
			for (String key : toRemove)
				Variables.setVariable(key, null, event, local);
		}
		return getNext();
	}

	@Override
	public Set<ExprInput<?>> getDependentInputs() {
		return dependentInputs;
	}

	@Override
	public @Nullable Object getCurrentValue() {
		return currentValue;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "filter " + unfilteredObjects.toString(event, debug) + " to match " + (isAny ? "any" : "all");
	}

}
