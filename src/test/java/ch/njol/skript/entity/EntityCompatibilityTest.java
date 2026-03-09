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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.AbstractThrownPotion;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
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
        EntityData<?> cow = EntityData.parse("cow");
        EntityData<?> boat = EntityData.parse("boat");
        EntityData<?> chestBoat = EntityData.parse("chest boat");
        EntityData<?> droppedItem = EntityData.parse("dropped item");
        EntityData<?> fallingBlock = EntityData.parse("falling block");
        EntityData<?> minecart = EntityData.parse("minecart");
        EntityData<?> mooshroom = EntityData.parse("mooshroom");
        EntityData<?> pluralZombie = EntityData.parse("zombies");
        EntityData<?> articleZombie = EntityData.parse("a zombie");
        EntityData<?> fishHook = EntityData.parse("fish hook");
        EntityData<?> strider = EntityData.parse("strider");
        EntityData<?> thrownPotion = EntityData.parse("thrown potion");
        EntityData<?> supertype = EntityData.parse("living entity");
        EntityData<?> pluralZombieVillager = EntityData.parse("zombie villagers");
        EntityData<?> tropicalFish = EntityData.parse("tropical fish");
        EntityData<?> xpOrb = EntityData.parse("xp orb");

        assertNotNull(zombie);
        assertNotNull(cow);
        assertNotNull(boat);
        assertNotNull(chestBoat);
        assertNotNull(droppedItem);
        assertNotNull(fallingBlock);
        assertNotNull(minecart);
        assertNotNull(mooshroom);
        assertSame(zombie, pluralZombie);
        assertSame(zombie, articleZombie);
        assertNotNull(fishHook);
        assertNotNull(strider);
        assertNotNull(thrownPotion);
        assertNotNull(supertype);
        assertNotNull(pluralZombieVillager);
        assertNotNull(tropicalFish);
        assertNotNull(xpOrb);
        assertInstanceOf(BoatData.class, boat);
        assertInstanceOf(BoatChestData.class, chestBoat);
        assertInstanceOf(CowData.class, cow);
        assertInstanceOf(DroppedItemData.class, droppedItem);
        assertInstanceOf(FallingBlockData.class, fallingBlock);
        assertInstanceOf(MinecartData.class, minecart);
        assertInstanceOf(MooshroomData.class, mooshroom);
        assertInstanceOf(StriderData.class, strider);
        assertInstanceOf(ThrownPotionData.class, thrownPotion);
        assertInstanceOf(ZombieVillagerData.class, pluralZombieVillager);
        assertInstanceOf(TropicalFishData.class, tropicalFish);
        assertInstanceOf(XpOrbData.class, xpOrb);
        assertTrue(supertype.getType().isAssignableFrom(Zombie.class));
    }

    @Test
    void entityDataSupportsClassInfoParsing() {
        Object parsed = Classes.parse("cow", EntityData.class, ch.njol.skript.lang.ParseContext.DEFAULT);

        assertNotNull(parsed);
        assertInstanceOf(EntityData.class, parsed);
        assertInstanceOf(CowData.class, parsed);
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

    @Test
    void entityDataFromClassPrefersImportedExactWrappers() {
        assertInstanceOf(BoatData.class, EntityData.fromClass(Boat.class));
        assertInstanceOf(BoatChestData.class, EntityData.fromClass(ChestBoat.class));
        assertInstanceOf(CowData.class, EntityData.fromClass(Cow.class));
        assertInstanceOf(DroppedItemData.class, EntityData.fromClass(net.minecraft.world.entity.item.ItemEntity.class));
        assertInstanceOf(FallingBlockData.class, EntityData.fromClass(FallingBlockEntity.class));
        assertInstanceOf(MinecartData.class, EntityData.fromClass(AbstractMinecart.class));
        assertInstanceOf(MooshroomData.class, EntityData.fromClass(MushroomCow.class));
        assertInstanceOf(StriderData.class, EntityData.fromClass(Strider.class));
        assertInstanceOf(ThrownPotionData.class, EntityData.fromClass(AbstractThrownPotion.class));
        assertInstanceOf(VillagerData.class, EntityData.fromClass(Villager.class));
        assertInstanceOf(WolfData.class, EntityData.fromClass(Wolf.class));
        assertInstanceOf(XpOrbData.class, EntityData.fromClass(ExperienceOrb.class));
        assertInstanceOf(ZombieVillagerData.class, EntityData.fromClass(ZombieVillager.class));
    }
}
