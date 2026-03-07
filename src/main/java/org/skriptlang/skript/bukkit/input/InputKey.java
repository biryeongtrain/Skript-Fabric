package org.skriptlang.skript.bukkit.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.entity.player.Input;

public enum InputKey {
    FORWARD("forward movement key"),
    BACKWARD("backward movement key"),
    RIGHT("right movement key"),
    LEFT("left movement key"),
    JUMP("jump key"),
    SNEAK("sneak key"),
    SPRINT("sprint key");

    private final String displayName;

    InputKey(String displayName) {
        this.displayName = displayName;
    }

    public boolean check(Input input) {
        return switch (this) {
            case FORWARD -> input.forward();
            case BACKWARD -> input.backward();
            case RIGHT -> input.right();
            case LEFT -> input.left();
            case JUMP -> input.jump();
            case SNEAK -> input.shift();
            case SPRINT -> input.sprint();
        };
    }

    public String displayName() {
        return displayName;
    }

    public static InputKey[] fromInput(Input input) {
        Input normalized = input != null ? input : Input.EMPTY;
        List<InputKey> pressed = new ArrayList<>(values().length);
        for (InputKey inputKey : values()) {
            if (inputKey.check(normalized)) {
                pressed.add(inputKey);
            }
        }
        return pressed.toArray(InputKey[]::new);
    }

    public static InputKey parse(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.trim().toLowerCase(Locale.ENGLISH)
                .replace('-', ' ')
                .replace('_', ' ')
                .replace(" the ", " ")
                .replaceAll("\\s+", " ");
        return switch (normalized) {
            case "forward", "forward key", "forward movement", "forward movement key" -> FORWARD;
            case "backward", "backward key", "backward movement", "backward movement key", "backwards", "backwards key", "backwards movement key" -> BACKWARD;
            case "right", "right key", "right movement", "right movement key" -> RIGHT;
            case "left", "left key", "left movement", "left movement key" -> LEFT;
            case "jump", "jump key" -> JUMP;
            case "sneak", "sneak key", "shift", "shift key" -> SNEAK;
            case "sprint", "sprint key" -> SPRINT;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
