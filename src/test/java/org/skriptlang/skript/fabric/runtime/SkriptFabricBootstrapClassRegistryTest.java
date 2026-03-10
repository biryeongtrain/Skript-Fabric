package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import org.junit.jupiter.api.Test;

class SkriptFabricBootstrapClassRegistryTest {

    @Test
    void bootstrapRegistersPureJavaCompatibilityClassesAndFunctions() {
        SkriptFabricBootstrap.bootstrap();

        assertNotNull(Classes.getExactClassInfo(Object.class));
        assertNotNull(Classes.getExactClassInfo(Number.class));
        assertNotNull(Classes.getExactClassInfo(Time.class));
        assertNotNull(Classes.getExactClassInfo(Timeperiod.class));
        assertNotNull(Classes.getExactClassInfo(Date.class));
        assertNotNull(Classes.getExactClassInfo(ch.njol.skript.classes.ClassInfo.class));
        assertNotNull(Functions.getFunction("date"));
        assertNotNull(Functions.getFunction("round"));
    }
}
