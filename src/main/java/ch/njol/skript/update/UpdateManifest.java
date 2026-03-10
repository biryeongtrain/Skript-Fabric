package ch.njol.skript.update;

import java.net.URL;

/**
 * Returned by an update checker when an update is available.
 */
public class UpdateManifest {

    public final String id;
    public final String date;
    public final String patchNotes;
    public final URL downloadUrl;

    public UpdateManifest(String id, String date, String patchNotes, URL downloadUrl) {
        this.id = id;
        this.date = date;
        this.patchNotes = patchNotes;
        this.downloadUrl = downloadUrl;
    }
}
