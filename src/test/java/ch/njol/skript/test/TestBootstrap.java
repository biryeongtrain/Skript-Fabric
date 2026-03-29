package ch.njol.skript.test;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import com.mojang.serialization.Lifecycle;

/**
 * Minecraft test bootstrap that also binds data components to item holders.
 * In 26.1, {@code Bootstrap.bootStrap()} alone is not sufficient — item holders
 * need their data components bound before {@link net.minecraft.world.item.ItemStack}
 * can be constructed.
 */
public final class TestBootstrap {

    private static volatile boolean done;

    private TestBootstrap() {
    }

    public static void bootstrap() {
        if (done) {
            return;
        }
        synchronized (TestBootstrap.class) {
            if (done) {
                return;
            }
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            bindDataComponents();
            done = true;
        }
    }

    @SuppressWarnings("unchecked")
    private static void bindDataComponents() {
        HolderLookup.Provider provider = new LenientProvider();
        for (DataComponentInitializers.PendingComponents<?> pending :
                BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(provider)) {
            pending.apply();
        }
        // Ensure all item holders have components bound (even if empty)
        BuiltInRegistries.ITEM.listElements().forEach(holder -> {
            if (!holder.areComponentsBound()) {
                holder.bindComponents(DataComponentMap.EMPTY);
            }
        });
    }

    /**
     * A HolderLookup.Provider that wraps built-in registries and returns
     * empty results for registries/tags that only exist in datapacks.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final class LenientProvider implements HolderLookup.Provider {

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
            return BuiltInRegistries.REGISTRY.stream()
                    .map(r -> (ResourceKey<? extends Registry<?>>) r.key());
        }

        @Override
        public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(
                ResourceKey<? extends Registry<? extends T>> registryKey) {
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
            if (registry != null) {
                return Optional.of(new LenientRegistryLookup<>((Registry<T>) registry));
            }
            // Return an empty lookup for datapack-only registries
            return Optional.of(new EmptyRegistryLookup<>(registryKey));
        }
    }

    /**
     * Wraps a real registry but returns empty tag sets for missing tags.
     */
    @SuppressWarnings("unchecked")
    private static final class LenientRegistryLookup<T> implements HolderLookup.RegistryLookup<T> {

        private final Registry<T> delegate;

        LenientRegistryLookup(Registry<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public ResourceKey<? extends Registry<? extends T>> key() {
            return delegate.key();
        }

        @Override
        public Lifecycle registryLifecycle() {
            return delegate.registryLifecycle();
        }

        @Override
        public Stream<Holder.Reference<T>> listElements() {
            return delegate.listElements();
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            return delegate.listTags();
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
            return delegate.get(key);
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
            Optional<HolderSet.Named<T>> result = delegate.get(tagKey);
            if (result.isPresent()) {
                return result;
            }
            // Return empty named tag set for missing tags
            return Optional.of(HolderSet.emptyNamed(delegate, tagKey));
        }
    }

    /**
     * Empty lookup for datapack-only registries (like damage_type).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final class EmptyRegistryLookup<T> implements HolderLookup.RegistryLookup<T> {

        private final ResourceKey<? extends Registry<? extends T>> key;

        EmptyRegistryLookup(ResourceKey<? extends Registry<? extends T>> key) {
            this.key = key;
        }

        @Override
        public ResourceKey<? extends Registry<? extends T>> key() {
            return key;
        }

        @Override
        public Lifecycle registryLifecycle() {
            return Lifecycle.stable();
        }

        @Override
        public Stream<Holder.Reference<T>> listElements() {
            return Stream.empty();
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            return Stream.empty();
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
            // Create a standalone holder reference for missing elements
            Holder.Reference<T> ref = Holder.Reference.createStandAlone(this, resourceKey);
            ref.bindComponents(DataComponentMap.EMPTY);
            return Optional.of(ref);
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
            return Optional.of((HolderSet.Named<T>) HolderSet.emptyNamed((HolderOwner) this, tagKey));
        }
    }
}
