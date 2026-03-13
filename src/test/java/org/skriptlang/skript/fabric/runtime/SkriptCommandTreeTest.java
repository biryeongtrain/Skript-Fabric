package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class SkriptCommandTreeTest {

    @Test
    void registersExpectedSubcommandsAndRoutesOperations() throws Exception {
        CommandDispatcher<TestSource> dispatcher = new CommandDispatcher<>();
        FakeScriptService service = new FakeScriptService();
        SkriptCommandTree.register(dispatcher, service, new TestAccess());

        assertNotNull(dispatcher.getRoot().getChild("skript"));
        assertNotNull(dispatcher.getRoot().getChild("skript").getChild("reload"));
        assertNotNull(dispatcher.getRoot().getChild("skript").getChild("enable"));
        assertNotNull(dispatcher.getRoot().getChild("skript").getChild("disable"));
        assertNotNull(dispatcher.getRoot().getChild("skript").getChild("list"));
        assertNotNull(dispatcher.getRoot().getChild("skript").getChild("help"));

        TestSource source = new TestSource(true);
        dispatcher.execute("skript reload all", source);
        dispatcher.execute("skript enable quests/daily", source);
        dispatcher.execute("skript disable all", source);
        dispatcher.execute("skript list", source);
        dispatcher.execute("skript help", source);

        assertEquals(List.of("reloadAll", "enable:quests/daily", "disableAll"), service.invocations);
        assertTrue(source.messages.stream().anyMatch(message -> message.contains("Reload all: 2 script(s) affected")));
        assertTrue(source.messages.stream().anyMatch(message -> message.contains("Enable quests/daily: 1 script(s) affected")));
        assertTrue(source.messages.stream().anyMatch(message -> message.contains("Loaded scripts (2): quests/daily, admin")));
        assertTrue(source.messages.stream().anyMatch(message -> message.contains("Usage: /skript reload all|scripts|<target>")));
    }

    @Test
    void suggestsTargetsFromService() throws Exception {
        CommandDispatcher<TestSource> dispatcher = new CommandDispatcher<>();
        FakeScriptService service = new FakeScriptService();
        SkriptCommandTree.register(dispatcher, service, new TestAccess());

        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("skript reload q", new TestSource(true))).get();
        List<String> texts = suggestions.getList().stream().map(suggestion -> suggestion.getText()).toList();
        assertTrue(texts.contains("quests/daily"));
    }

    private static final class TestSource {
        private final boolean allowed;
        private final List<String> messages = new ArrayList<>();

        private TestSource(boolean allowed) {
            this.allowed = allowed;
        }
    }

    private static final class TestAccess implements SkriptCommandTree.SourceAccess<TestSource> {

        @Override
        public boolean canUse(TestSource source) {
            return source.allowed;
        }

        @Override
        public void success(TestSource source, String message) {
            source.messages.add(message);
        }

        @Override
        public void failure(TestSource source, String message) {
            source.messages.add("FAIL:" + message);
        }
    }

    private static final class FakeScriptService implements SkriptScriptService {

        private final List<String> invocations = new ArrayList<>();

        @Override
        public Path root() {
            return Path.of("config", "skript");
        }

        @Override
        public List<String> discoverScripts() {
            return List.of("quests/daily.sk", "admin.sk");
        }

        @Override
        public SkriptScriptOperationResult loadAll() {
            invocations.add("loadAll");
            return new SkriptScriptOperationResult(2, List.of("quests/daily", "admin"));
        }

        @Override
        public SkriptScriptOperationResult unloadAll() {
            invocations.add("unloadAll");
            return new SkriptScriptOperationResult(2, List.of("quests/daily", "admin"));
        }

        @Override
        public SkriptScriptOperationResult reloadAll() {
            invocations.add("reloadAll");
            return new SkriptScriptOperationResult(2, List.of("quests/daily", "admin"));
        }

        @Override
        public SkriptScriptOperationResult reloadScripts() {
            invocations.add("reloadScripts");
            return new SkriptScriptOperationResult(2, List.of("quests/daily", "admin"));
        }

        @Override
        public SkriptScriptOperationResult reloadTarget(String target) {
            invocations.add("reload:" + target);
            return new SkriptScriptOperationResult(1, List.of(target));
        }

        @Override
        public SkriptScriptOperationResult enableAll() {
            invocations.add("enableAll");
            return new SkriptScriptOperationResult(2, List.of("quests/daily", "admin"));
        }

        @Override
        public SkriptScriptOperationResult enableTarget(String target) {
            invocations.add("enable:" + target);
            return new SkriptScriptOperationResult(1, List.of(target));
        }

        @Override
        public SkriptScriptOperationResult disableAll() {
            invocations.add("disableAll");
            return new SkriptScriptOperationResult(2, List.of("quests/daily", "admin"));
        }

        @Override
        public SkriptScriptOperationResult disableTarget(String target) {
            invocations.add("disable:" + target);
            return new SkriptScriptOperationResult(1, List.of(target));
        }

        @Override
        public List<String> listLoadedScripts() {
            return List.of("quests/daily", "admin");
        }

        @Override
        public List<String> suggestedTargets() {
            return List.of("quests/daily", "admin");
        }

        @Override
        public void shutdown() {
        }
    }
}
