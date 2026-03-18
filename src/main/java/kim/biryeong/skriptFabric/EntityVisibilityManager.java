package kim.biryeong.skriptFabric;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-viewer entity visibility state.
 * Thread-safe via ConcurrentHashMap.
 */
public final class EntityVisibilityManager {

    private static final EntityVisibilityManager INSTANCE = new EntityVisibilityManager();

    private final Set<UUID> globallyHidden = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Set<UUID>> hiddenFor = new ConcurrentHashMap<>();

    private EntityVisibilityManager() {
    }

    public static EntityVisibilityManager instance() {
        return INSTANCE;
    }

    public boolean isHidden(UUID entityId, UUID viewerId) {
        if (globallyHidden.contains(entityId)) {
            return true;
        }
        Set<UUID> viewers = hiddenFor.get(entityId);
        return viewers != null && viewers.contains(viewerId);
    }

    public void hideGlobally(UUID entityId) {
        globallyHidden.add(entityId);
    }

    public void revealGlobally(UUID entityId) {
        globallyHidden.remove(entityId);
        hiddenFor.remove(entityId);
    }

    public void hideFor(UUID entityId, UUID viewerId) {
        hiddenFor.computeIfAbsent(entityId, k -> ConcurrentHashMap.newKeySet()).add(viewerId);
    }

    public void revealFor(UUID entityId, UUID viewerId) {
        Set<UUID> viewers = hiddenFor.get(entityId);
        if (viewers != null) {
            viewers.remove(viewerId);
            if (viewers.isEmpty()) {
                hiddenFor.remove(entityId);
            }
        }
    }

    public boolean isGloballyHidden(UUID entityId) {
        return globallyHidden.contains(entityId);
    }

    public Set<UUID> getHiddenViewers(UUID entityId) {
        Set<UUID> viewers = hiddenFor.get(entityId);
        return viewers != null ? Collections.unmodifiableSet(viewers) : Collections.emptySet();
    }
}
