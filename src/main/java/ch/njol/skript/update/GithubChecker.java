package ch.njol.skript.update;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * Uses Github API to check for updates.
 */
public class GithubChecker implements UpdateChecker {

    public static class ResponseEntry {

        public String url;
        public String assets_url;
        public String upload_url;
        public String html_url;
        public int id;
        public String tag_name;
        public String target_commitish;
        public String name;
        public boolean draft;
        public boolean prerelease;
        public String created_at;
        public String published_at;
        public List<AssetsEntry> assets;
        public String body;
        public Author author;

        public static class AssetsEntry {

            public int size;
            public int download_count;
            public String browser_download_url;
        }

        public static class Author {

            public String login;
            public int id;
        }

        @Override
        public String toString() {
            return tag_name;
        }
    }

    private final Gson gson;

    public GithubChecker() {
        this.gson = new Gson();
    }

    private List<ResponseEntry> deserialize(String str) {
        assert str != null : "Cannot deserialize null string";
        Type listType = new TypeToken<List<ResponseEntry>>() { }.getType();
        List<ResponseEntry> responses = gson.fromJson(str, listType);
        assert responses != null;
        return responses;
    }

    @Override
    public CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel) {
        return CompletableFuture.supplyAsync(() -> {
            URL url;
            try {
                url = new URL(manifest.updateSource);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            try (Scanner scan = new Scanner(url.openStream(), "UTF-8")) {
                String out = scan.useDelimiter("\\A").next();
                assert out != null;
                return deserialize(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).thenApply(releases -> {
            ResponseEntry latest = null;
            ResponseEntry current = null;
            for (ResponseEntry release : releases) {
                String name = release.tag_name;
                assert name != null;
                if (latest == null && channel.check(name)) {
                    latest = release;
                }
                if (manifest.id.equals(name)) {
                    current = release;
                    break;
                }
            }
            if (latest == null) {
                return null;
            }
            if (current != null && latest.id == current.id) {
                return null;
            }
            if (latest.assets == null || latest.assets.isEmpty()) {
                return null;
            }
            try {
                String name = latest.tag_name;
                assert name != null;
                String createdAt = latest.created_at;
                assert createdAt != null;
                String patchNotes = latest.body;
                assert patchNotes != null;
                URL download = manifest.downloadSource != null
                        ? new URL(manifest.downloadSource)
                        : new URL(latest.assets.get(0).browser_download_url);
                return new UpdateManifest(name, createdAt, patchNotes, download);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
