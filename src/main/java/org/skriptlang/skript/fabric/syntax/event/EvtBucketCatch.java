package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftRegistryLookup;
import org.skriptlang.skript.fabric.runtime.FabricBucketCatchEventHandle;

public final class EvtBucketCatch extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on bucket catch",
            "on bucket catching",
            "on bucket capture",
            "on bucket capturing",
            "on bucket catch [of] %-objects%",
            "on bucket catching [of] %-objects%",
            "on bucket capture [of] %-objects%",
            "on bucket capturing [of] %-objects%"
    };

    private @Nullable EntityType<?>[] entityTypes;

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (matchedPattern == 0 || args.length == 0 || args[0] == null) {
            entityTypes = null;
            return true;
        }
        entityTypes = parseEntityTypes(args[0]);
        return entityTypes.length > 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricBucketCatchEventHandle handle)) {
            return false;
        }
        if (entityTypes == null || entityTypes.length == 0) {
            return true;
        }
        EntityType<?> caughtType = handle.bucketedEntity().getType();
        return Arrays.stream(entityTypes).anyMatch(type -> type == caughtType);
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricBucketCatchEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return entityTypes == null || entityTypes.length == 0
                ? "on bucket catch"
                : "on bucket catch of " + Classes.toString(entityTypes, false);
    }

    private static EntityType<?>[] parseEntityTypes(Literal<?> literal) {
        List<EntityType<?>> parsed = new ArrayList<>();
        for (Object raw : literal.getArray(null)) {
            if (raw == null) {
                continue;
            }
            EntityType<?> entityType = raw instanceof EntityType<?> direct
                    ? direct
                    : parseEntityType(String.valueOf(raw));
            if (entityType == null) {
                return new EntityType[0];
            }
            parsed.add(entityType);
        }
        return parsed.toArray(EntityType[]::new);
    }

    private static @Nullable EntityType<?> parseEntityType(String raw) {
        return MinecraftRegistryLookup.lookup(raw, EvtBucketCatch::entityTypeFromId);
    }

    private static @Nullable EntityType<?> entityTypeFromId(Identifier id) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        Identifier key = entityType == null ? null : BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return id.equals(key) ? entityType : null;
    }
}
