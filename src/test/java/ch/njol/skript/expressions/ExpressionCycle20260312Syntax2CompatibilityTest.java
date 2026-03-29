package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260312Syntax2CompatibilityTest {

    private static boolean helperConditionsRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        if (!helperConditionsRegistered) {
            Skript.registerCondition(TestConstantCondition.class, "lane-s2-always", "lane-s2-never");
            helperConditionsRegistered = true;
        }
    }

    @Test
    void vectorExpressionsFollowLegacyGeometrySemantics() {
        assertVec3(ExprVectorCylindrical.fromCylindrical(2.0D, 0.0F, 3.0D), 0.0D, 3.0D, 2.0D);
        assertVec3(ExprVectorCylindrical.fromCylindrical(-2.0D, 0.0F, 3.0D), 0.0D, 3.0D, 2.0D);
        assertVec3(ExprVectorFromYawAndPitch.fromYawAndPitch(0.0F, 0.0F), 0.0D, 0.0D, 1.0D);

        ExprVectorSpherical spherical = new ExprVectorSpherical();
        assertTrue(spherical.init(new Expression[]{
                new SimpleLiteral<>(-2.0D, false),
                new SimpleLiteral<>(0.0D, false),
                new SimpleLiteral<>(0.0D, false)
        }, 0, Kleenean.FALSE, parseResult("")));
        assertVec3(spherical.getSingle(SkriptEvent.EMPTY), 0.0D, 0.0D, 2.0D);

        ExprVectorFromDirection fromDirection = new ExprVectorFromDirection();
        assertTrue(fromDirection.init(new Expression[]{
                new SimpleLiteral<>(new Direction(net.minecraft.core.Direction.UP, 2.0D), false)
        }, 0, Kleenean.FALSE, parseResult("")));
        assertVec3(fromDirection.getSingle(SkriptEvent.EMPTY), 0.0D, 2.0D, 0.0D);

        ExprVectorFromDirection restrictedPattern = new ExprVectorFromDirection();
        assertFalse(restrictedPattern.init(new Expression[]{
                new SimpleLiteral<>(new Direction(Vec3.ZERO), false)
        }, 1, Kleenean.FALSE, parseResult("")));
    }

    @Test
    void stringCaseMatchesUpstreamModeConversions() {
        ExprStringCase uppercase = new ExprStringCase();
        assertTrue(uppercase.init(new Expression[]{new SimpleLiteral<>("Oops!", false)}, 0, Kleenean.FALSE, parseResult("")));
        assertEquals("OOPS!", uppercase.getSingle(SkriptEvent.EMPTY));

        ExprStringCase strictProper = new ExprStringCase();
        SkriptParser.ParseResult properParse = parseResult("");
        properParse.mark = 1;
        assertTrue(strictProper.init(new Expression[]{new SimpleLiteral<>("hellO i'm steve!", false)}, 3, Kleenean.FALSE, properParse));
        assertEquals("Hello I'm Steve!", strictProper.getSingle(SkriptEvent.EMPTY));

        ExprStringCase strictCamel = new ExprStringCase();
        SkriptParser.ParseResult camelParse = parseResult("");
        camelParse.mark = 1;
        assertTrue(strictCamel.init(new Expression[]{new SimpleLiteral<>("spAwn neW boSs ()", false)}, 5, Kleenean.FALSE, camelParse));
        assertEquals("spawnNewBoss()", strictCamel.getSingle(SkriptEvent.EMPTY));

        ExprStringCase lowerSnake = new ExprStringCase();
        SkriptParser.ParseResult snakeParse = parseResult("");
        snakeParse.mark = 1;
        assertTrue(lowerSnake.init(new Expression[]{new SimpleLiteral<>("Hello Player!", false)}, 9, Kleenean.FALSE, snakeParse));
        assertEquals("hello_player!", lowerSnake.getSingle(SkriptEvent.EMPTY));

        ExprStringCase upperKebab = new ExprStringCase();
        SkriptParser.ParseResult kebabParse = parseResult("");
        kebabParse.mark = 2;
        assertTrue(upperKebab.init(new Expression[]{new SimpleLiteral<>("What is your name?", false)}, 11, Kleenean.FALSE, kebabParse));
        assertEquals("WHAT-IS-YOUR-NAME?", upperKebab.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void colouredRawStringAndStringColorPreserveFormattingContracts() {
        ExprColoured coloured = new ExprColoured();
        assertTrue(coloured.init(new Expression[]{new SimpleLiteral<>("&aHello <#12ab34>world", false)}, 0, Kleenean.FALSE, parseResult("")));
        assertEquals("§aHello §x§1§2§a§b§3§4world", coloured.getSingle(SkriptEvent.EMPTY));

        ExprRawString rawString = new ExprRawString();
        assertTrue(rawString.init(new Expression[]{new SimpleLiteral<>("§aHello §x§1§2§a§b§3§4world", false)}, 0, Kleenean.FALSE, parseResult("")));
        assertEquals("&aHello <#12ab34>world", rawString.getSingle(SkriptEvent.EMPTY));

        ExprRawString rejectingRaw = new ExprRawString();
        assertFalse(rejectingRaw.init(new Expression[]{coloured}, 0, Kleenean.FALSE, parseResult("")));

        ExprStringColor colors = new ExprStringColor();
        assertTrue(colors.init(new Expression[]{new SimpleLiteral<>("&aHello <#12ab34>world", false)}, 0, Kleenean.FALSE, parseResult("")));
        Object[] allColors = colors.getArray(SkriptEvent.EMPTY);
        assertEquals(2, allColors.length);
        assertEquals(new ColorRGB(85, 255, 85), allColors[0]);
        assertEquals(new ColorRGB(18, 171, 52), allColors[1]);

        ExprStringColor lastCode = new ExprStringColor();
        SkriptParser.ParseResult codeParse = parseResult("");
        codeParse.tags.add("code");
        assertTrue(lastCode.init(new Expression[]{new SimpleLiteral<>("&aHello <#12ab34>world", false)}, 2, Kleenean.FALSE, codeParse));
        assertEquals("§x§1§2§a§b§3§4", lastCode.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void ternaryEvaluatesConditionsAndRejectsNesting() {
        ExprTernary ternary = new ExprTernary();
        assertTrue(ternary.init(new Expression[]{
                new SimpleLiteral<>(1, false),
                new SimpleLiteral<>(2, false)
        }, 0, Kleenean.FALSE, conditionParseResult("lane-s2-always")));
        assertEquals(1, ternary.getSingle(SkriptEvent.EMPTY));

        ExprTernary falseBranch = new ExprTernary();
        assertTrue(falseBranch.init(new Expression[]{
                new SimpleLiteral<>("yes", false),
                new SimpleLiteral<>("no", false)
        }, 0, Kleenean.FALSE, conditionParseResult("lane-s2-never")));
        assertEquals("no", falseBranch.getSingle(SkriptEvent.EMPTY));

        ExprTernary nested = new ExprTernary();
        assertFalse(nested.init(new Expression[]{
                new ExprTernary(),
                new SimpleLiteral<>(0, false)
        }, 0, Kleenean.FALSE, conditionParseResult("lane-s2-always")));
    }

    @Test
    void hostnameTpsAndPermissionsMatchFabricRuntimeContracts() {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("connect", ClientIntentionPacket.class);
            ExprHostname hostname = new ExprHostname();
            assertTrue(hostname.init(new Expression[0], 0, Kleenean.FALSE, parseResult("hostname")));
            ClientIntentionPacket packet = new ClientIntentionPacket(765, "testers.example.com", 25565, ClientIntent.LOGIN);
            assertEquals("testers.example.com", hostname.getSingle(new SkriptEvent(packet, null, null, null)));
            assertNull(hostname.getSingle(SkriptEvent.EMPTY));
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }

        ExprTPS tps = new ExprTPS();
        assertTrue(tps.init(new Expression[0], 3, Kleenean.FALSE, parseResult("tps")));
        assertEquals(0, tps.getArray(SkriptEvent.EMPTY).length);
        assertEquals(20.0D, ExprTPS.resolveTpsFromAverageTickTimeNanos(50_000_000L), 0.00001D);
        assertEquals(10.0D, ExprTPS.resolveTpsFromAverageTickTimeNanos(100_000_000L), 0.00001D);

        LinkedHashMap<String, Boolean> permissionMap = new LinkedHashMap<>();
        permissionMap.put("skript.tree", true);
        permissionMap.put("skript.tree.*", false);
        permissionMap.put("skript.forest", true);
        assertEquals(
                java.util.List.of("skript.forest", "skript.tree"),
                java.util.List.of(ExprPermissions.collectPermissionKeys(permissionMap))
        );
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static SkriptParser.ParseResult conditionParseResult(String condition) {
        SkriptParser.ParseResult result = parseResult(condition);
        result.regexes = new java.util.ArrayList<>();
        Matcher matcher = Pattern.compile(".+", Pattern.DOTALL).matcher(condition);
        assertTrue(matcher.matches());
        result.regexes.add(matcher);
        return result;
    }

    private static void assertVec3(@Nullable Vec3 vector, double x, double y, double z) {
        assertNotNull(vector);
        assertEquals(x, vector.x, 0.00001D);
        assertEquals(y, vector.y, 0.00001D);
        assertEquals(z, vector.z, 0.00001D);
    }

    private static void restoreEventContext(ParserInstance parser, @Nullable String previousEventName, @Nullable Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }
    }

    public static final class TestConstantCondition extends Condition {

        private boolean result;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            if (expressions.length != 0) {
                return false;
            }
            result = matchedPattern == 0;
            return true;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return result;
        }
    }
}
