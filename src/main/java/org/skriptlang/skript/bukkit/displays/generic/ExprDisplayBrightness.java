package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayBrightness extends SimpleExpression<Integer> {

    private Expression<Entity> displays;
    private boolean blockLight;
    private boolean skyLight;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        displays = (Expression<Entity>) expressions[0];
        blockLight = matchedPattern == 0;
        skyLight = matchedPattern == 1;
        return true;
    }

    @Override
    protected Integer @Nullable [] get(SkriptEvent event) {
        List<Integer> values = new ArrayList<>();
        for (Entity entity : displays.getAll(event)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            Brightness brightness = PrivateEntityAccess.displayBrightnessOverride(display);
            if (brightness == null) {
                continue;
            }
            if (skyLight) {
                values.add(brightness.sky());
            } else if (blockLight) {
                values.add(brightness.block());
            } else {
                values.add(brightness.block());
                values.add(brightness.sky());
            }
        }
        return values.toArray(Integer[]::new);
    }

    @Override
    public boolean isSingle() {
        return (skyLight || blockLight) && displays.isSingle();
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (skyLight || blockLight) {
            return switch (mode) {
                case ADD, REMOVE, SET -> new Class[]{Integer.class, Number.class};
                default -> null;
            };
        }
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{Integer.class, Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (skyLight || blockLight) {
            int amount = delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.intValue() : 0;
            for (Entity entity : displays.getAll(event)) {
                if (!(entity instanceof Display display)) {
                    continue;
                }
                Brightness current = PrivateEntityAccess.displayBrightnessOverride(display);
                int currentBlock = current == null ? amount : current.block();
                int currentSky = current == null ? amount : current.sky();
                Brightness next = switch (mode) {
                    case ADD -> skyLight
                            ? new Brightness(currentBlock, clampBrightness(currentSky + amount))
                            : new Brightness(clampBrightness(currentBlock + amount), currentSky);
                    case REMOVE -> skyLight
                            ? new Brightness(currentBlock, clampBrightness(currentSky - amount))
                            : new Brightness(clampBrightness(currentBlock - amount), currentSky);
                    case SET -> skyLight
                            ? new Brightness(current == null ? clampBrightness(amount) : current.block(), clampBrightness(amount))
                            : new Brightness(clampBrightness(amount), current == null ? clampBrightness(amount) : current.sky());
                    default -> current;
                };
                if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET) {
                    PrivateEntityAccess.setDisplayBrightnessOverride(display, next);
                }
            }
            return;
        }

        if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            for (Entity entity : displays.getAll(event)) {
                if (entity instanceof Display display) {
                    PrivateEntityAccess.setDisplayBrightnessOverride(display, null);
                }
            }
            return;
        }

        int level = delta != null && delta.length > 0 && delta[0] instanceof Number number ? clampBrightness(number.intValue()) : 0;
        Brightness next = new Brightness(level, level);
        for (Entity entity : displays.getAll(event)) {
            if (entity instanceof Display display) {
                PrivateEntityAccess.setDisplayBrightnessOverride(display, next);
            }
        }
    }

    private int clampBrightness(int value) {
        return Math.max(0, Math.min(15, value));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (skyLight) {
            return "sky light override of " + displays.toString(event, debug);
        }
        if (blockLight) {
            return "block light override of " + displays.toString(event, debug);
        }
        return "brightness override of " + displays.toString(event, debug);
    }
}
