package ch.njol.skript.classes.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SkriptClassesCompatibilityTest {

    @AfterEach
    void cleanup() {
        Classes.clearClassInfos();
    }

    @Test
    void registerAddsSerializerFreeSkriptClassInfos() {
        JavaClasses.register();

        SkriptClasses.register();
        SkriptClasses.register();

        ClassInfo<ClassInfo> classInfo = Classes.getExactClassInfo(ClassInfo.class);
        assertNotNull(classInfo);
        assertSame(classInfo, classInfo.getParser().parse("type", ParseContext.DEFAULT));

        ClassInfo<Time> timeInfo = Classes.getExactClassInfo(Time.class);
        assertNotNull(timeInfo);
        Time parsedTime = timeInfo.getParser().parse("8 pm", ParseContext.DEFAULT);
        assertNotNull(parsedTime);
        assertEquals("20:00", parsedTime.toString());

        ClassInfo<Timeperiod> timeperiodInfo = Classes.getExactClassInfo(Timeperiod.class);
        assertNotNull(timeperiodInfo);
        assertEquals(new Timeperiod(13800, 22199), timeperiodInfo.getParser().parse("night", ParseContext.DEFAULT));
        assertEquals(new Timeperiod(Time.parse("6:30").getTicks()), timeperiodInfo.getParser().parse("6:30", ParseContext.DEFAULT));

        assertNotNull(Classes.getExactClassInfo(Date.class));
        assertNotNull(Classes.getExactClassInfo(ch.njol.skript.lang.function.DynamicFunctionReference.class));
    }
}
