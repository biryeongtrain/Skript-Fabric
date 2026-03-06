package ch.njol.skript.lang;

import org.skriptlang.skript.registration.SyntaxInfo;

public class SyntaxElementInfo<E extends SyntaxElement> {

    private final String[] patterns;
    private final Class<E> elementClass;
    private final String originClassPath;

    public SyntaxElementInfo(String[] patterns, Class<E> elementClass, String originClassPath) {
        this.patterns = patterns;
        this.elementClass = elementClass;
        this.originClassPath = originClassPath;
    }

    public SyntaxElementInfo(SyntaxInfo<E> source) {
        this(source.patterns(), source.type(), source.originClassPath());
    }

    public String[] getPatterns() {
        return patterns;
    }

    public Class<E> getElementClass() {
        return elementClass;
    }

    public String getOriginClassPath() {
        return originClassPath;
    }

    public static <E extends SyntaxElement> SyntaxElementInfo<E> fromModern(SyntaxInfo<E> source) {
        return new SyntaxElementInfo<>(source);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static SyntaxElementInfo<?> fromModernRaw(SyntaxInfo<?> source) {
        return new SyntaxElementInfo(source);
    }
}
