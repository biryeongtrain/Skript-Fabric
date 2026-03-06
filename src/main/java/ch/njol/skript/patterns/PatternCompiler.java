package ch.njol.skript.patterns;

public final class PatternCompiler {

    private PatternCompiler() {
    }

    public static SkriptPattern compile(String pattern) {
        return new SkriptPattern(pattern);
    }
}
