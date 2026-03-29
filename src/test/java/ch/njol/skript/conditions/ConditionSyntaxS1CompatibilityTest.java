package ch.njol.skript.conditions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.fabric.runtime.FabricEggThrowEventHandle;

class ConditionSyntaxS1CompatibilityTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		TestBootstrap.bootstrap();
	}

	@AfterEach
	void cleanupRegistry() {
		Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
		ParserInstance.get().deleteCurrentEvent();
	}

	@Test
	void s1ConditionsInstantiate() {
		assertDoesNotThrow(CondItemEnchantmentGlint::new);
		assertDoesNotThrow(CondIsFuel::new);
		assertDoesNotThrow(CondIsOfType::new);
		assertDoesNotThrow(CondIsResonating::new);
		assertDoesNotThrow(CondEntityStorageIsFull::new);
		assertDoesNotThrow(CondWillHatch::new);
	}

	@Test
	void s1ConditionsPreserveLegacyToStringShapes() {
		CondItemEnchantmentGlint glintOverride = new CondItemEnchantmentGlint();
		glintOverride.init(new Expression[]{new TestExpression<>("item", FabricItemType.class)}, 0, Kleenean.FALSE, parseResult(""));
		assertEquals("item has enchantment glint overridden", glintOverride.toString(SkriptEvent.EMPTY, false));

		CondItemEnchantmentGlint forcedNot = new CondItemEnchantmentGlint();
		SkriptParser.ParseResult forcedParse = parseResult("");
		forcedParse.tags.add("not");
		forcedNot.init(new Expression[]{new TestExpression<>("items", FabricItemType.class)}, 2, Kleenean.FALSE, forcedParse);
		assertEquals("items are forced to not glint", forcedNot.toString(SkriptEvent.EMPTY, false));

		CondIsFuel fuel = new CondIsFuel();
		fuel.init(new Expression[]{new TestExpression<>("tool", FabricItemType.class)}, 0, Kleenean.FALSE, parseResult(""));
		assertEquals("tool is fuel", fuel.toString(SkriptEvent.EMPTY, false));

		CondIsOfType ofType = new CondIsOfType();
		ofType.init(new Expression[]{
			new TestExpression<>("tool", ItemStack.class),
			new TestExpression<>("stick", FabricItemType.class)
		}, 0, Kleenean.FALSE, parseResult(""));
		assertEquals("tool is of type stick", ofType.toString(SkriptEvent.EMPTY, false));

		CondEntityStorageIsFull storage = new CondEntityStorageIsFull();
		storage.init(new Expression[]{new TestExpression<>("beehive", Object.class)}, 0, Kleenean.FALSE, parseResult(""));
		assertEquals("the entity storage of beehive is full", storage.toString(SkriptEvent.EMPTY, false));

		CondWillHatch hatch = new CondWillHatch();
		ParserInstance.get().setCurrentEvent("egg throw", eggThrowEventClass());
		SkriptParser.ParseResult hatchParse = parseResult("");
		hatchParse.tags.add("will");
		assertTrue(hatch.init(new Expression[0], 0, Kleenean.FALSE, hatchParse));
		assertEquals("the egg will hatch", hatch.toString(SkriptEvent.EMPTY, false));
	}

	@Test
	void s1ConditionsRetainBasicRuntimeChecks() {
		ItemStack overridden = new ItemStack(Items.STICK);
		overridden.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		CondItemEnchantmentGlint glintOverride = new CondItemEnchantmentGlint();
		glintOverride.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(overridden), false)}, 0, Kleenean.FALSE, parseResult(""));
		assertTrue(glintOverride.check(SkriptEvent.EMPTY));

		CondItemEnchantmentGlint forcedGlint = new CondItemEnchantmentGlint();
		forcedGlint.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(overridden), false)}, 2, Kleenean.FALSE, parseResult(""));
		assertTrue(forcedGlint.check(SkriptEvent.EMPTY));

		CondItemEnchantmentGlint forcedNotGlint = new CondItemEnchantmentGlint();
		ItemStack noGlint = new ItemStack(Items.STICK);
		noGlint.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
		SkriptParser.ParseResult notParse = parseResult("");
		notParse.tags.add("not");
		forcedNotGlint.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(noGlint), false)}, 2, Kleenean.FALSE, notParse);
		assertTrue(forcedNotGlint.check(SkriptEvent.EMPTY));

		CondIsFuel fuel = new CondIsFuel();
		fuel.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.COAL), false)}, 0, Kleenean.FALSE, parseResult(""));
		assertTrue(fuel.check(SkriptEvent.EMPTY));

		CondIsFuel notFuel = new CondIsFuel();
		notFuel.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STONE), false)}, 1, Kleenean.FALSE, parseResult(""));
		assertTrue(notFuel.check(SkriptEvent.EMPTY));

		CondIsOfType itemType = new CondIsOfType();
		itemType.init(new Expression[]{
			new SimpleLiteral<>(new ItemStack(Items.STICK), false),
			new SimpleLiteral<>(new FabricItemType(Items.STICK), false)
		}, 0, Kleenean.FALSE, parseResult(""));
		assertTrue(itemType.check(SkriptEvent.EMPTY));

		CondWillHatch hatch = new CondWillHatch();
		ParserInstance.get().setCurrentEvent("egg throw", eggThrowEventClass());
		SkriptParser.ParseResult hatchParse = parseResult("");
		hatchParse.tags.add("will");
		assertTrue(hatch.init(new Expression[0], 0, Kleenean.FALSE, hatchParse));
		assertTrue(hatch.check(new SkriptEvent(new TestEggThrowHandle(true, (byte) 1), null, null, null)));

		CondWillHatch noHatch = new CondWillHatch();
		SkriptParser.ParseResult noHatchParse = parseResult("");
		assertTrue(noHatch.init(new Expression[0], 0, Kleenean.FALSE, noHatchParse));
		assertTrue(noHatch.check(new SkriptEvent(new TestEggThrowHandle(false, (byte) 0), null, null, null)));
	}

	@Test
	void willHatchRequiresEggThrowMarker() {
		CondWillHatch hatch = new CondWillHatch();
		assertFalse(hatch.init(new Expression[0], 0, Kleenean.FALSE, parseResult("")));
	}

	private static Class<?> eggThrowEventClass() {
		try {
			return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerEggThrow");
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static SkriptParser.ParseResult parseResult(String expr) {
		SkriptParser.ParseResult result = new SkriptParser.ParseResult();
		result.expr = expr;
		return result;
	}

	private record TestEggThrowHandle(boolean hatching, byte hatches) implements FabricEggThrowEventHandle {
		@Override
		public @Nullable ThrownEgg egg() {
			return null;
		}

		@Override
		public void setHatching(boolean hatching) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setHatches(byte hatches) {
			throw new UnsupportedOperationException();
		}

		@Override
		public EntityType<?> hatchingType() {
			return EntityType.CHICKEN;
		}

		@Override
		public void setHatchingType(EntityType<?> hatchingType) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class TestExpression<T> extends SimpleExpression<T> {

		private final String text;
		private final Class<? extends T> returnType;

		private TestExpression(String text, Class<? extends T> returnType) {
			this.text = text;
			this.returnType = returnType;
		}

		@Override
		protected T @Nullable [] get(SkriptEvent event) {
			@SuppressWarnings("unchecked")
			T[] empty = (T[]) java.lang.reflect.Array.newInstance(returnType, 0);
			return empty;
		}

		@Override
		public boolean isSingle() {
			return !text.endsWith("s");
		}

		@Override
		public Class<? extends T> getReturnType() {
			return returnType;
		}

		@Override
		public String toString(@Nullable SkriptEvent event, boolean debug) {
			return text;
		}
	}
}
