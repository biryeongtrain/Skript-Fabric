package ch.njol.skript.update;

import java.util.concurrent.CompletableFuture;

/**
 * Checks for updates.
 */
public interface UpdateChecker {

    CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel);
}
