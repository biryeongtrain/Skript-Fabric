package ch.njol.skript.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class Version implements Serializable, Comparable<Version> {

    private static final long serialVersionUID = 8687040355286333293L;
    public static final Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(.*))?");

    private final Integer[] version = new Integer[3];
    private final @Nullable String postfix;

    public Version(int... version) {
        if (version.length < 1 || version.length > 3) {
            throw new IllegalArgumentException(
                    "Versions must have a minimum of 2 and a maximum of 3 numbers (" + version.length + " numbers given)"
            );
        }
        for (int i = 0; i < version.length; i++) {
            this.version[i] = version[i];
        }
        this.postfix = null;
    }

    public Version(int major, int minor, @Nullable String postfix) {
        version[0] = major;
        version[1] = minor;
        this.postfix = postfix == null || postfix.isEmpty() ? null : postfix;
    }

    public Version(String version) {
        Matcher matcher = versionPattern.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("'" + version + "' is not a valid version string");
        }
        for (int i = 0; i < 3; i++) {
            if (matcher.group(i + 1) != null) {
                this.version[i] = Utils.parseInt(matcher.group(i + 1));
            }
        }
        this.postfix = matcher.group(4);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Version other)) {
            return false;
        }
        return compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(version) * 31 + (postfix == null ? 0 : postfix.hashCode());
    }

    @Override
    public int compareTo(@Nullable Version other) {
        if (other == null) {
            return 1;
        }
        for (int i = 0; i < version.length; i++) {
            if (get(i) > other.get(i)) {
                return 1;
            }
            if (get(i) < other.get(i)) {
                return -1;
            }
        }
        return comparePostfixes(postfix, other.postfix);
    }

    private static int comparePostfixes(@Nullable String postfixA, @Nullable String postfixB) {
        postfixA = postfixA == null ? null : postfixA.toLowerCase();
        postfixB = postfixB == null ? null : postfixB.toLowerCase();
        if (postfixA == null && postfixB == null) {
            return 0;
        }
        if (postfixA == null) {
            return postfixB.startsWith("nightly") ? -1 : 1;
        }
        if (postfixB == null) {
            return postfixA.startsWith("nightly") ? 1 : -1;
        }
        if (postfixA.startsWith("nightly")) {
            return postfixB.startsWith("nightly") ? 0 : 1;
        }
        if (postfixB.startsWith("nightly")) {
            return -1;
        }
        String[] prefixes = {"pre", "beta", "alpha"};
        for (String prefix : prefixes) {
            boolean aStarts = postfixA.startsWith(prefix);
            boolean bStarts = postfixB.startsWith(prefix);
            if (!aStarts && !bStarts) {
                continue;
            }
            if (aStarts && bStarts) {
                int aNumber = Math.abs(Utils.parseInt(postfixA.substring(prefix.length()).trim()));
                int bNumber = Math.abs(Utils.parseInt(postfixB.substring(prefix.length()).trim()));
                return Integer.compare(aNumber, bNumber);
            }
            return aStarts ? 1 : -1;
        }
        return 0;
    }

    public int compareTo(int... other) {
        for (int i = 0; i < version.length; i++) {
            int otherValue = i >= other.length ? 0 : other[i];
            if (get(i) > otherValue) {
                return 1;
            }
            if (get(i) < otherValue) {
                return -1;
            }
        }
        return 0;
    }

    private int get(int index) {
        return version[index] == null ? 0 : version[index];
    }

    public boolean isSmallerThan(Version other) {
        return compareTo(other) < 0;
    }

    public boolean isLargerThan(Version other) {
        return compareTo(other) > 0;
    }

    public boolean isStable() {
        return postfix == null;
    }

    public int getMajor() {
        return version[0];
    }

    public int getMinor() {
        return version[1];
    }

    public int getRevision() {
        return version[2] == null ? 0 : version[2];
    }

    @Override
    public String toString() {
        return version[0] + "." + version[1] + (version[2] == null ? "" : "." + version[2])
                + (postfix == null ? "" : "-" + postfix);
    }

    public static int compare(String left, String right) {
        return new Version(left).compareTo(new Version(right));
    }
}
