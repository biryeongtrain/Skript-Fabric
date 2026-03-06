package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public class SectionSkriptEvent extends SkriptEvent {

    private final String name;
    private final Section section;

    public SectionSkriptEvent(String name, Section section) {
        this.name = name;
        this.section = section;
    }

    public Section getSection() {
        return section;
    }

    public final boolean isSection(Class<? extends Section> sectionClass) {
        return sectionClass.isInstance(section);
    }

    @SafeVarargs
    public final boolean isSection(Class<? extends Section>... sectionClasses) {
        for (Class<? extends Section> sectionClass : sectionClasses) {
            if (isSection(sectionClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        throw new SkriptAPIException("init should never be called for a SectionSkriptEvent.");
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        throw new SkriptAPIException("check should never be called for a SectionSkriptEvent.");
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return name;
    }
}
