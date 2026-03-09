package ch.njol.skript.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.registrations.Classes;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.monster.Zombie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class EntityCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        EntityData.register();
        EntityType.register();
    }

    @Test
    void entityDataParsesRegistryNamesAliasesAndSupertypes() {
        EntityData<?> zombie = EntityData.parse("zombie");
        EntityData<?> pluralZombie = EntityData.parse("zombies");
        EntityData<?> articleZombie = EntityData.parse("a zombie");
        EntityData<?> fishHook = EntityData.parse("fish hook");
        EntityData<?> supertype = EntityData.parse("living entity");

        assertNotNull(zombie);
        assertSame(zombie, pluralZombie);
        assertSame(zombie, articleZombie);
        assertNotNull(fishHook);
        assertNotNull(supertype);
        assertTrue(supertype.getType().isAssignableFrom(Zombie.class));
    }

    @Test
    void entityDataSupportsClassInfoParsing() {
        Object parsed = Classes.parse("cow", EntityData.class, ch.njol.skript.lang.ParseContext.DEFAULT);

        assertNotNull(parsed);
        assertInstanceOf(EntityData.class, parsed);
        assertEquals("cow", parsed.toString());
    }

    @Test
    void entityTypeParsesAmountsAndArticles() {
        EntityType plural = EntityType.parse("2 zombies");
        EntityType singular = EntityType.parse("a zombie");

        assertNotNull(plural);
        assertNotNull(singular);
        assertEquals(2, plural.getAmount());
        assertEquals(1, singular.getAmount());
        assertEquals("2 zombie", plural.toString());
        assertEquals("zombie", singular.toString());
    }

    @Test
    void entityTypeSupportsClassInfoParsing() {
        Object parsed = Classes.parse("3 cows", EntityType.class, ch.njol.skript.lang.ParseContext.DEFAULT);

        assertNotNull(parsed);
        assertInstanceOf(EntityType.class, parsed);
        assertEquals("3 cow", parsed.toString());
    }

    @Test
    void unknownEntityNamesFailParsing() {
        assertNull(EntityData.parse("not a real entity"));
        assertNull(EntityType.parse("12 definitely fake entities"));
    }
}
