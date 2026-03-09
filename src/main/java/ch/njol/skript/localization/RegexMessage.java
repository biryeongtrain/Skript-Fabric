package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;

public class RegexMessage extends Message {

    public static final Pattern nop = Pattern.compile("(?!)");

    private final String prefix;
    private final String suffix;
    private final int flags;
    private @Nullable Pattern pattern;

    public RegexMessage(String key, @Nullable String prefix, @Nullable String suffix, int flags) {
        super(key);
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.flags = flags;
    }

    public RegexMessage(String key, String prefix, String suffix) {
        this(key, prefix, suffix, 0);
    }

    public RegexMessage(String key, int flags) {
        this(key, "", "", flags);
    }

    public RegexMessage(String key) {
        this(key, "", "", 0);
    }

    public @Nullable Pattern getPattern() {
        validate();
        return pattern;
    }

    public Matcher matcher(String input) {
        Pattern compiled = getPattern();
        return compiled == null ? nop.matcher(input) : compiled.matcher(input);
    }

    public boolean matches(String input) {
        Pattern compiled = getPattern();
        return compiled != null && compiled.matcher(input).matches();
    }

    public boolean find(String input) {
        Pattern compiled = getPattern();
        return compiled != null && compiled.matcher(input).find();
    }

    @Override
    public String toString() {
        validate();
        return prefix + getValue() + suffix;
    }

    @Override
    protected void onValueChange() {
        try {
            pattern = Pattern.compile(prefix + getValue() + suffix, flags);
        } catch (PatternSyntaxException e) {
            Skript.error("Invalid Regex pattern '" + getValue() + "' found at '" + key
                    + "' in the " + Language.getName() + " language file: " + e.getLocalizedMessage());
            pattern = null;
        }
    }
}
