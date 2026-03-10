package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceTypeSupport;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.FabricDamageSourceEventHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxRegistry;
import sun.misc.Unsafe;

class ConditionImportedRuntimeCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void cleanupRegistryAndParser() throws ReflectiveOperationException {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
        ParserInstance.get().deleteCurrentEvent();
        ParserInstance.get().setCurrentScript(null);
        setLoadedScripts(List.of());
    }

    @Test
    void importedConditionsInstantiate() {
        assertDoesNotThrow(CondCancelled::new);
        assertDoesNotThrow(CondDamageCause::new);
        assertDoesNotThrow(CondEntityUnload::new);
        assertDoesNotThrow(CondIncendiary::new);
        assertDoesNotThrow(CondItemDespawn::new);
        assertDoesNotThrow(CondIsPreferredTool::new);
        assertDoesNotThrow(CondIsSedated::new);
        assertDoesNotThrow(CondScriptLoaded::new);
        assertDoesNotThrow(CondLeashWillDrop::new);
        assertDoesNotThrow(CondRespawnLocation::new);
    }

    @Test
    void eventBoundConditionsKeepExpectedChecksAndStrings() {
        CondCancelled cancelled = new CondCancelled();
        cancelled.init(new Expression[0], 0, Kleenean.FALSE, parseResult(""));
        assertTrue(cancelled.check(new SkriptEvent(new CancelledHandle(true), null, null, null)));
        assertEquals("event is cancelled", cancelled.toString(SkriptEvent.EMPTY, false));

        DamageSource lava = newDamageSource();
        String damageName = DamageSourceTypeSupport.display(lava);
        CondDamageCause damageCause = new CondDamageCause();
        damageCause.init(new Expression[]{new SimpleLiteral<>(damageName, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(damageCause.check(new SkriptEvent(new DamageHandle(lava), null, null, null)));
        assertEquals("damage was caused by [" + damageName + "]", damageCause.toString(SkriptEvent.EMPTY, false));

        ParserInstance.get().setCurrentEvent("explosion prime", eventClass("ExplosionPrime"));
        CondIncendiary incendiary = new CondIncendiary();
        assertTrue(incendiary.init(new Expression[0], 2, Kleenean.FALSE, parseResult("")));
        assertTrue(incendiary.check(new SkriptEvent(new ExplosionHandle(true), null, null, null)));
        assertEquals("the event-explosion is incendiary", incendiary.toString(SkriptEvent.EMPTY, false));

        ParserInstance.get().setCurrentEvent("unleash", eventClass("EntityUnleash"));
        CondLeashWillDrop leashWillDrop = new CondLeashWillDrop();
        assertTrue(leashWillDrop.init(new Expression[0], 0, Kleenean.FALSE, parseResult("")));
        assertTrue(leashWillDrop.check(new SkriptEvent(new UnleashHandle(true), null, null, null)));
        assertEquals("the leash will be dropped", leashWillDrop.toString(SkriptEvent.EMPTY, false));

        ParserInstance.get().setCurrentEvent("respawn", eventClass("PlayerRespawn"));
        CondRespawnLocation bed = new CondRespawnLocation();
        SkriptParser.ParseResult bedParse = parseResult("");
        bedParse.tags.add("bed");
        assertTrue(bed.init(new Expression[0], 0, Kleenean.FALSE, bedParse));
        assertTrue(bed.check(new SkriptEvent(new RespawnHandle(true, false), null, null, null)));
        assertEquals("the respawn location is a bed spawn", bed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void scriptLoadedTracksCurrentScriptAndNamedScriptLookup() throws ReflectiveOperationException {
        Script current = new Script(new Config("lane-e-current", "lane-e-current.sk", null), new ArrayList<>());
        Script other = new Script(new Config("nested/other", "nested/other.sk", null), new ArrayList<>());
        setLoadedScripts(List.of(current, other));
        ParserInstance.get().setCurrentScript(current);

        CondScriptLoaded implicit = new CondScriptLoaded();
        assertTrue(implicit.init(new Expression[0], 0, Kleenean.FALSE, parseResult("")));
        assertTrue(implicit.check(SkriptEvent.EMPTY));
        assertEquals("script is loaded", implicit.toString(SkriptEvent.EMPTY, false));

        CondScriptLoaded named = new CondScriptLoaded();
        named.init(new Expression[]{new SimpleLiteral<>("nested/other.sk", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(named.check(SkriptEvent.EMPTY));

        CondScriptLoaded missing = new CondScriptLoaded();
        missing.init(new Expression[]{new SimpleLiteral<>("missing.sk", false)}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(missing.check(SkriptEvent.EMPTY));
    }

    @Test
    void importedConditionsPreserveReadableLegacyToStrings() {
        CondEntityUnload entityUnload = new CondEntityUnload();
        entityUnload.init(new Expression[]{new TestExpression<>("mob", Object.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("mob can despawn on chunk unload", entityUnload.toString(SkriptEvent.EMPTY, false));

        CondItemDespawn itemDespawn = new CondItemDespawn();
        itemDespawn.init(new Expression[]{new TestExpression<>("drops", Object.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("drops will be naturally despawn", itemDespawn.toString(SkriptEvent.EMPTY, false));

        CondIsPreferredTool preferredTool = new CondIsPreferredTool();
        preferredTool.init(new Expression[]{
                new TestExpression<>("tool", FabricItemType.class),
                new TestExpression<>("ore", Object.class)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("tool is the preferred tool for ore", preferredTool.toString(SkriptEvent.EMPTY, false));

        CondIsSedated sedated = new CondIsSedated();
        sedated.init(new Expression[]{new TestExpression<>("beehive", Object.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("beehive is sedated", sedated.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void eventRestrictedConditionsRejectWrongContext() {
        CondLeashWillDrop leash = new CondLeashWillDrop();
        assertFalse(leash.init(new Expression[0], 0, Kleenean.FALSE, parseResult("")));

        CondRespawnLocation respawn = new CondRespawnLocation();
        assertFalse(respawn.init(new Expression[0], 0, Kleenean.FALSE, parseResult("")));

        CondIncendiary incendiary = new CondIncendiary();
        assertFalse(incendiary.init(new Expression[0], 2, Kleenean.FALSE, parseResult("")));
    }

    private static void setLoadedScripts(List<Script> scripts) throws ReflectiveOperationException {
        Field field = SkriptRuntime.class.getDeclaredField("scripts");
        field.setAccessible(true);
        field.set(SkriptRuntime.instance(), new ArrayList<>(scripts));
    }

    private static Class<?> eventClass(String simpleName) {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$" + simpleName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static DamageSource newDamageSource() {
        try {
            LivingEntity source = allocateEntity(net.minecraft.world.entity.monster.Zombie.class, EntityType.ZOMBIE);
            Constructor<DamageSource> constructor = DamageSource.class.getDeclaredConstructor(
                    Holder.class,
                    net.minecraft.world.entity.Entity.class,
                    net.minecraft.world.entity.Entity.class,
                    net.minecraft.world.phys.Vec3.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance((Holder<DamageType>) null, source, source, null);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends LivingEntity> T allocateEntity(Class<T> type, EntityType<?> entityType) throws ReflectiveOperationException {
        Unsafe unsafe = unsafe();
        T entity = (T) unsafe.allocateInstance(type);
        Field entityTypeField = null;
        for (Field field : net.minecraft.world.entity.Entity.class.getDeclaredFields()) {
            if (field.getType() == EntityType.class) {
                entityTypeField = field;
                break;
            }
        }
        if (entityTypeField == null) {
            throw new IllegalStateException("Could not find entity type field");
        }
        entityTypeField.setAccessible(true);
        entityTypeField.set(entity, entityType);
        return entity;
    }

    private static Unsafe unsafe() throws ReflectiveOperationException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private record CancelledHandle(boolean isCancelled) {
    }

    private record DamageHandle(DamageSource damageSource) implements FabricDamageSourceEventHandle {
    }

    private record ExplosionHandle(boolean causesFire) {
    }

    private record UnleashHandle(boolean isDropLeash) {
    }

    private record RespawnHandle(boolean isBedSpawn, boolean isAnchorSpawn) {
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
