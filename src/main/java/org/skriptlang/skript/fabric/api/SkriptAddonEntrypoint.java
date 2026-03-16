package org.skriptlang.skript.fabric.api;

/**
 * Entrypoint interface for Fabric mods that extend Skript with custom syntax.
 *
 * <p>Implementations are discovered via Fabric's custom entrypoint mechanism
 * using the key {@code "skript"} in {@code fabric.mod.json}. The
 * {@link #onSkriptInitialize()} method is called after Skript's core bootstrap
 * completes but before any scripts are loaded.</p>
 *
 * <p>Within {@link #onSkriptInitialize()}, addons should:</p>
 * <ul>
 *   <li>Register custom event handler classes via {@code Skript.registerEvent(...)}</li>
 *   <li>Register event values via {@code EventValues.registerEventValue(...)}</li>
 *   <li>Register conditions, effects, expressions via their respective {@code Skript.registerXxx()} methods</li>
 *   <li>Hook Fabric event callbacks that dispatch to the Skript runtime via {@link SkriptEventDispatcher}</li>
 * </ul>
 *
 * <h3>Example usage in {@code fabric.mod.json}:</h3>
 * <pre>{@code
 * {
 *   "entrypoints": {
 *     "skript": ["com.example.myaddon.MySkriptAddon"]
 *   },
 *   "depends": {
 *     "skfabric": "*"
 *   }
 * }
 * }</pre>
 */
public interface SkriptAddonEntrypoint {

    /**
     * Called once during mod initialization, after Skript core bootstrap is
     * complete. All core types, conditions, effects, and expressions are
     * already registered. Registration APIs are open and accepting new syntax.
     *
     * <p>This method is always called on the main server thread.</p>
     *
     * <p>If this method throws an exception, the error is logged and other
     * addons continue to initialize normally.</p>
     */
    void onSkriptInitialize();
}
