package ch.njol.skript.classes.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.comparator.Relation;

class RegistryCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void cleanup() {
        Classes.clearClassInfos();
        Language.clear();
    }

    @Test
    void registryParserUsesLocalizedAndRegistryIdLookups() {
        Language.loadDefault(java.util.Map.of(
                "items.stone", "stone block, stone",
                "items.dirt", "dirt block, dirt"
        ));
        RegistryParser<Item> parser = new RegistryParser<>(BuiltInRegistries.ITEM, "items");

        assertSame(Items.STONE, parser.parse("stone block", ch.njol.skript.lang.ParseContext.DEFAULT));
        assertSame(Items.STONE, parser.parse("stone", ch.njol.skript.lang.ParseContext.DEFAULT));
        assertSame(Items.STONE, parser.parse("minecraft:stone", ch.njol.skript.lang.ParseContext.DEFAULT));
        assertEquals("stone block", parser.toString(Items.STONE, 0));
        java.util.List<String> patterns = java.util.List.of(parser.getPatterns());
        assertTrue(patterns.contains("stone"));
        assertTrue(patterns.contains("stone block"));
        assertTrue(patterns.contains("dirt"));
        assertTrue(patterns.contains("dirt block"));
    }

    @Test
    void registryClassInfoRegistersParserSupplierAndComparator() {
        Language.loadDefault(java.util.Map.of(
                "items.stone", "stone",
                "items.dirt", "dirt"
        ));
        RegistryClassInfo<Item> info = new RegistryClassInfo<>(Item.class, BuiltInRegistries.ITEM, "item", "items");

        Classes.registerClassInfo(info);

        assertNotNull(info.getParser());
        assertNotNull(info.getSupplier());
        assertSame(Items.STONE, info.getParser().parse("stone", ch.njol.skript.lang.ParseContext.DEFAULT));
        assertEquals(Relation.EQUAL, ch.njol.skript.registrations.Comparators.compare(Items.STONE, Items.STONE));
        assertEquals(Relation.NOT_EQUAL, ch.njol.skript.registrations.Comparators.compare(Items.STONE, Items.DIRT));
    }

    @Test
    void registrySerializerRoundTripsRegistryIds() throws Exception {
        RegistrySerializer<Item> serializer = new RegistrySerializer<>(BuiltInRegistries.ITEM);

        assertEquals("minecraft:stone", serializer.serialize(Items.STONE));
        assertSame(Items.STONE, serializer.deserialize("minecraft:stone"));
        assertSame(Items.STONE, serializer.deserialize("stone"));
    }
}
