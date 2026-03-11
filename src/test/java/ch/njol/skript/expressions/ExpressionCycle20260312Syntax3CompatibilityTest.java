package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Timespan;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;
import sun.misc.Unsafe;

class ExpressionCycle20260312Syntax3CompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
    }

    @Test
    void villagerLevelHelpersClampExpectedRanges() {
        ExprVillagerLevel level = new ExprVillagerLevel();
        assertTrue(level.init(new Expression[]{new TypedExpression<>(LivingEntity.class)}, 0, ch.njol.util.Kleenean.FALSE, parseResult("")));
        assertArrayEquals(new Class[]{Number.class}, level.acceptChange(ChangeMode.SET));

        ExprVillagerLevel experience = new ExprVillagerLevel();
        SkriptParser.ParseResult experienceParse = parseResult("");
        experienceParse.tags.add("experience");
        assertTrue(experience.init(new Expression[]{new TypedExpression<>(LivingEntity.class)}, 0, ch.njol.util.Kleenean.FALSE, experienceParse));

        assertEquals(5, ExprVillagerLevel.applyVillagerValue(1, 10, ChangeMode.SET, false));
        assertEquals(1, ExprVillagerLevel.applyVillagerValue(2, 10, ChangeMode.REMOVE, false));
        assertEquals(0, ExprVillagerLevel.applyVillagerValue(3, 10, ChangeMode.REMOVE, true));
        assertEquals("villager experience of TypedExpression", experience.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void villagerProfessionTypeAndTimePlayedContractsExposeExpectedTypes() {
        ExprVillagerProfession profession = new ExprVillagerProfession();
        assertEquals(VillagerProfession.class, profession.getReturnType());
        assertArrayEquals(new Class[]{VillagerProfession.class}, profession.acceptChange(ChangeMode.DELETE));

        ExprVillagerType type = new ExprVillagerType();
        assertEquals(VillagerType.class, type.getReturnType());
        assertArrayEquals(new Class[]{VillagerType.class}, type.acceptChange(ChangeMode.SET));

        ExprTimePlayed timePlayed = new ExprTimePlayed();
        assertTrue(timePlayed.init(new Expression[]{new TypedExpression<>(GameProfile.class)}, 0, ch.njol.util.Kleenean.FALSE, parseResult("")));
        assertArrayEquals(new Class[]{Timespan.class}, timePlayed.acceptChange(ChangeMode.ADD));
        assertEquals("time played of TypedExpression", timePlayed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void totalExperienceHelpersMatchUpstreamMath() throws Exception {
        assertEquals(17, ExprTotalExperience.levelExperience(5));
        assertEquals(160, ExprTotalExperience.cumulativeExperience(10));

        ServerPlayer player = allocate(ServerPlayer.class);
        player.experienceLevel = 12;
        player.experienceProgress = 0.5F;
        assertEquals(
                ExprTotalExperience.cumulativeExperience(12) + Math.round(ExprTotalExperience.levelExperience(12) * 0.5F),
                ExprTotalExperience.totalExperience(player)
        );

        ExprTotalExperience.setTotalExperience(player, 250);
        assertEquals(250, player.totalExperience);
        assertEquals(250, ExprTotalExperience.totalExperience(player));
    }

    @Test
    void timePlayedHelpersReadAndWriteStatsCounters() throws Exception {
        Path directory = Files.createTempDirectory("syntax3-time-played");
        try {
            Path file = directory.resolve("player.json");
            ServerStatsCounter stats = new ServerStatsCounter(null, file.toFile());
            ExprTimePlayed.writeTicks(stats, null, 120);
            assertEquals(120, ExprTimePlayed.readTicks(stats));

            ExprTimePlayed.writeTicks(stats, null, -25);
            assertEquals(0, ExprTimePlayed.readTicks(stats));
        } finally {
            Files.walk(directory)
                    .sorted((left, right) -> right.getNameCount() - left.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }

    @Test
    void itemTransformsProduceCompatItems() {
        ExprUnbreakable unbreakable = new ExprUnbreakable();
        SkriptParser.ParseResult unbreakableParse = parseResult("");
        unbreakableParse.tags.add("un");
        unbreakable.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.IRON_SWORD), false)}, 0, ch.njol.util.Kleenean.FALSE, unbreakableParse);
        assertEquals(Unit.INSTANCE, unbreakable.getSingle(SkriptEvent.EMPTY).toStack().get(DataComponents.UNBREAKABLE));
    }

    @Test
    void loreReadsAndMutatesItemStacksAndCompatItemTypes() {
        ItemStack stack = new ItemStack(Items.STICK);

        ExprLore lore = new ExprLore();
        lore.init(new Expression[]{new SimpleLiteral<>(stack, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult(""));
        lore.change(SkriptEvent.EMPTY, new Object[]{"line 1\nline 2"}, ChangeMode.SET);
        assertArrayEquals(new String[]{"line 1", "line 2"}, lore.getArray(SkriptEvent.EMPTY));

        ExprLore secondLine = new ExprLore();
        secondLine.init(new Expression[]{
                new SimpleLiteral<>(2, false),
                new SimpleLiteral<>(stack, false)
        }, 2, ch.njol.util.Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"line 2"}, secondLine.getArray(SkriptEvent.EMPTY));
        secondLine.change(SkriptEvent.EMPTY, new Object[]{"!"}, ChangeMode.ADD);
        assertArrayEquals(new String[]{"line 2!"}, secondLine.getArray(SkriptEvent.EMPTY));

        FabricItemType itemType = new FabricItemType(Items.APPLE);
        ExprLore itemTypeLore = new ExprLore();
        itemTypeLore.init(new Expression[]{new SimpleLiteral<>(itemType, false)}, 0, ch.njol.util.Kleenean.FALSE, parseResult(""));
        itemTypeLore.change(SkriptEvent.EMPTY, new Object[]{"fresh"}, ChangeMode.SET);
        assertEquals(List.of(Component.literal("fresh")), itemType.toStack().get(DataComponents.LORE).lines());
        itemTypeLore.change(SkriptEvent.EMPTY, new Object[]{"re"}, ChangeMode.REMOVE);
        assertArrayEquals(new String[]{"fsh"}, itemTypeLore.getArray(SkriptEvent.EMPTY));
        itemTypeLore.change(SkriptEvent.EMPTY, null, ChangeMode.DELETE);
        assertArrayEquals(new String[0], itemTypeLore.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void ownedExpressionsInstantiate() {
        assertDoesNotThrow(ExprLore::new);
        assertDoesNotThrow(ExprTimePlayed::new);
        assertDoesNotThrow(ExprTotalExperience::new);
        assertDoesNotThrow(ExprUnbreakable::new);
        assertDoesNotThrow(ExprVillagerLevel::new);
        assertDoesNotThrow(ExprVillagerProfession::new);
        assertDoesNotThrow(ExprVillagerType::new);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        return (T) unsafe().allocateInstance(type);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class TypedExpression<T> implements Expression<T> {

        private final Class<? extends T> type;

        private TypedExpression(Class<? extends T> type) {
            this.type = type;
        }

        @Override
        public Class<? extends T> getReturnType() {
            return type;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "TypedExpression";
        }
    }
}
