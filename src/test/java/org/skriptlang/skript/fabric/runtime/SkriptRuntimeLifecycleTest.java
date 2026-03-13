package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

final class SkriptRuntimeLifecycleTest {

    @Test
    void serverStartedLoadsScriptsAndStoppingShutsThemDown() {
        FakeScriptService service = new FakeScriptService();

        SkriptRuntimeLifecycle.onServerStarted(service, LoggerFactory.getLogger("runtime-lifecycle-test"));
        SkriptRuntimeLifecycle.onServerStopping(service, LoggerFactory.getLogger("runtime-lifecycle-test"));

        assertEquals(List.of("loadAll", "shutdown"), service.invocations);
    }

    private static final class FakeScriptService implements SkriptScriptService {

        private final List<String> invocations = new ArrayList<>();

        @Override
        public Path root() {
            return Path.of("config", "skript");
        }

        @Override
        public List<String> discoverScripts() {
            return List.of();
        }

        @Override
        public SkriptScriptOperationResult loadAll() throws IOException {
            invocations.add("loadAll");
            return new SkriptScriptOperationResult(2, List.of("admin", "quests/daily"));
        }

        @Override
        public SkriptScriptOperationResult unloadAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SkriptScriptOperationResult reloadAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SkriptScriptOperationResult reloadScripts() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SkriptScriptOperationResult reloadTarget(String target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SkriptScriptOperationResult enableAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SkriptScriptOperationResult enableTarget(String target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SkriptScriptOperationResult disableAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SkriptScriptOperationResult disableTarget(String target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> listLoadedScripts() {
            return List.of();
        }

        @Override
        public List<String> suggestedTargets() {
            return List.of();
        }

        @Override
        public void shutdown() {
            invocations.add("shutdown");
        }
    }
}
