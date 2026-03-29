package org.skriptlang.skript.bukkit.damagesource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public final class DamageSourceTypeSupport {

    private DamageSourceTypeSupport() {
    }

    public static String display(DamageSource damageSource) {
        try {
            return damageSource.typeHolder()
                    .unwrapKey()
                    .map(ResourceKey::identifier)
                    .map(MinecraftResourceParser::display)
                    .orElseGet(() -> fallbackDisplay(damageSource));
        } catch (RuntimeException ignored) {
            return fallbackDisplay(damageSource);
        }
    }

    private static String fallbackDisplay(DamageSource damageSource) {
        try {
            return damageSource.getMsgId();
        } catch (RuntimeException ignored) {
            return "generic";
        }
    }

    public static @Nullable Holder<DamageType> parseHolder(@Nullable Object value, @Nullable ServerLevel level) {
        if (value instanceof DamageSource damageSource) {
            return damageSource.typeHolder();
        }
        if (value instanceof Holder<?> holder && holder.value() instanceof DamageType) {
            @SuppressWarnings("unchecked") Holder<DamageType> cast = (Holder<DamageType>) holder;
            return cast;
        }
        if (!(value instanceof String string) || level == null) {
            return null;
        }
        try {
            Identifier id = MinecraftResourceParser.parse(string.trim());
            Object registryLookup = invoke(level.registryAccess(), "lookupOrThrow", Registries.DAMAGE_TYPE);
            Object key = ResourceKey.create(Registries.DAMAGE_TYPE, id);
            Object holder = tryInvoke(registryLookup, "get", key);
            if (holder == null) {
                holder = tryInvoke(registryLookup, "getOrThrow", key);
            }
            if (holder instanceof Holder<?> parsed && parsed.value() instanceof DamageType) {
                @SuppressWarnings("unchecked") Holder<DamageType> cast = (Holder<DamageType>) parsed;
                return cast;
            }
        } catch (RuntimeException | ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }

    private static Object invoke(Object target, String method, Object argument) throws ReflectiveOperationException {
        Method declared = target.getClass().getMethod(method, argument.getClass());
        declared.setAccessible(true);
        return declared.invoke(target, argument);
    }

    private static @Nullable Object tryInvoke(Object target, String method, Object argument)
            throws IllegalAccessException, InvocationTargetException {
        for (Method candidate : target.getClass().getMethods()) {
            if (!candidate.getName().equals(method) || candidate.getParameterCount() != 1) {
                continue;
            }
            candidate.setAccessible(true);
            return candidate.invoke(target, argument);
        }
        return null;
    }
}
