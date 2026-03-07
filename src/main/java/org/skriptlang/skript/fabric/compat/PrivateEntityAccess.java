package org.skriptlang.skript.fabric.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.mojang.math.Transformation;
import net.minecraft.util.Brightness;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.item.ItemDisplayContext;

public final class PrivateEntityAccess {

    private static final Method DISPLAY_GET_BILLBOARD_CONSTRAINTS = lookup(Display.class, "getBillboardConstraints", Display.BillboardConstraints.class);
    private static final Method DISPLAY_SET_BILLBOARD_CONSTRAINTS = lookup(Display.class, "setBillboardConstraints", void.class, Display.BillboardConstraints.class);
    private static final Method DISPLAY_GET_BRIGHTNESS_OVERRIDE = lookup(Display.class, "getBrightnessOverride", Brightness.class);
    private static final Method DISPLAY_SET_BRIGHTNESS_OVERRIDE = lookup(Display.class, "setBrightnessOverride", void.class, Brightness.class);
    private static final Method DISPLAY_GET_VIEW_RANGE = lookup(Display.class, "getViewRange", float.class);
    private static final Method DISPLAY_SET_VIEW_RANGE = lookup(Display.class, "setViewRange", void.class, float.class);
    private static final Method DISPLAY_GET_TRANSFORMATION_INTERPOLATION_DURATION = lookup(Display.class, "getTransformationInterpolationDuration", int.class);
    private static final Method DISPLAY_SET_TRANSFORMATION_INTERPOLATION_DURATION = lookup(Display.class, "setTransformationInterpolationDuration", void.class, int.class);
    private static final Method DISPLAY_GET_TRANSFORMATION_INTERPOLATION_DELAY = lookup(Display.class, "getTransformationInterpolationDelay", int.class);
    private static final Method DISPLAY_SET_TRANSFORMATION_INTERPOLATION_DELAY = lookup(Display.class, "setTransformationInterpolationDelay", void.class, int.class);
    private static final Method DISPLAY_GET_POS_ROT_INTERPOLATION_DURATION = lookup(Display.class, "getPosRotInterpolationDuration", int.class);
    private static final Method DISPLAY_SET_POS_ROT_INTERPOLATION_DURATION = lookup(Display.class, "setPosRotInterpolationDuration", void.class, int.class);
    private static final Method DISPLAY_GET_SHADOW_RADIUS = lookup(Display.class, "getShadowRadius", float.class);
    private static final Method DISPLAY_SET_SHADOW_RADIUS = lookup(Display.class, "setShadowRadius", void.class, float.class);
    private static final Method DISPLAY_GET_SHADOW_STRENGTH = lookup(Display.class, "getShadowStrength", float.class);
    private static final Method DISPLAY_SET_SHADOW_STRENGTH = lookup(Display.class, "setShadowStrength", void.class, float.class);
    private static final Method DISPLAY_GET_WIDTH = lookup(Display.class, "getWidth", float.class);
    private static final Method DISPLAY_SET_WIDTH = lookup(Display.class, "setWidth", void.class, float.class);
    private static final Method DISPLAY_GET_HEIGHT = lookup(Display.class, "getHeight", float.class);
    private static final Method DISPLAY_SET_HEIGHT = lookup(Display.class, "setHeight", void.class, float.class);
    private static final Method DISPLAY_GET_GLOW_COLOR_OVERRIDE = lookup(Display.class, "getGlowColorOverride", int.class);
    private static final Method DISPLAY_SET_GLOW_COLOR_OVERRIDE = lookup(Display.class, "setGlowColorOverride", void.class, int.class);
    private static final Method DISPLAY_GET_TRANSFORMATION = lookup(Display.class, "createTransformation", Transformation.class, net.minecraft.network.syncher.SynchedEntityData.class);
    private static final Method DISPLAY_SET_TRANSFORMATION = lookup(Display.class, "setTransformation", void.class, Transformation.class);
    private static final Method TEXT_DISPLAY_GET_FLAGS = lookup(Display.TextDisplay.class, "getFlags", byte.class);
    private static final Method TEXT_DISPLAY_SET_FLAGS = lookup(Display.TextDisplay.class, "setFlags", void.class, byte.class);
    private static final Method TEXT_DISPLAY_GET_LINE_WIDTH = lookup(Display.TextDisplay.class, "getLineWidth", int.class);
    private static final Method TEXT_DISPLAY_SET_LINE_WIDTH = lookup(Display.TextDisplay.class, "setLineWidth", void.class, int.class);
    private static final Method TEXT_DISPLAY_GET_TEXT_OPACITY = lookup(Display.TextDisplay.class, "getTextOpacity", byte.class);
    private static final Method TEXT_DISPLAY_SET_TEXT_OPACITY = lookup(Display.TextDisplay.class, "setTextOpacity", void.class, byte.class);
    private static final Method TEXT_DISPLAY_GET_TEXT = lookup(Display.TextDisplay.class, "getText", Component.class);
    private static final Method TEXT_DISPLAY_SET_TEXT = lookup(Display.TextDisplay.class, "setText", void.class, Component.class);
    private static final Method ITEM_DISPLAY_GET_ITEM_TRANSFORM = lookup(Display.ItemDisplay.class, "getItemTransform", ItemDisplayContext.class);
    private static final Method ITEM_DISPLAY_SET_ITEM_TRANSFORM = lookup(Display.ItemDisplay.class, "setItemTransform", void.class, ItemDisplayContext.class);
    private static final Method INTERACTION_GET_WIDTH = lookup(Interaction.class, "getWidth", float.class);
    private static final Method INTERACTION_SET_WIDTH = lookup(Interaction.class, "setWidth", void.class, float.class);
    private static final Method INTERACTION_GET_HEIGHT = lookup(Interaction.class, "getHeight", float.class);
    private static final Method INTERACTION_SET_HEIGHT = lookup(Interaction.class, "setHeight", void.class, float.class);
    private static final Method INTERACTION_GET_RESPONSE = lookup(Interaction.class, "getResponse", boolean.class);
    private static final Method INTERACTION_SET_RESPONSE = lookup(Interaction.class, "setResponse", void.class, boolean.class);

    private PrivateEntityAccess() {
    }

    public static Display.BillboardConstraints displayBillboardConstraints(Display display) {
        return invoke(DISPLAY_GET_BILLBOARD_CONSTRAINTS, display);
    }

    public static void setDisplayBillboardConstraints(Display display, Display.BillboardConstraints constraints) {
        invoke(DISPLAY_SET_BILLBOARD_CONSTRAINTS, display, constraints);
    }

    public static Brightness displayBrightnessOverride(Display display) {
        return invoke(DISPLAY_GET_BRIGHTNESS_OVERRIDE, display);
    }

    public static void setDisplayBrightnessOverride(Display display, Brightness brightness) {
        invoke(DISPLAY_SET_BRIGHTNESS_OVERRIDE, display, brightness);
    }

    public static float displayViewRange(Display display) {
        return invoke(DISPLAY_GET_VIEW_RANGE, display);
    }

    public static void setDisplayViewRange(Display display, float viewRange) {
        invoke(DISPLAY_SET_VIEW_RANGE, display, viewRange);
    }

    public static int displayTransformationInterpolationDuration(Display display) {
        return invoke(DISPLAY_GET_TRANSFORMATION_INTERPOLATION_DURATION, display);
    }

    public static void setDisplayTransformationInterpolationDuration(Display display, int ticks) {
        invoke(DISPLAY_SET_TRANSFORMATION_INTERPOLATION_DURATION, display, ticks);
    }

    public static int displayTransformationInterpolationDelay(Display display) {
        return invoke(DISPLAY_GET_TRANSFORMATION_INTERPOLATION_DELAY, display);
    }

    public static void setDisplayTransformationInterpolationDelay(Display display, int ticks) {
        invoke(DISPLAY_SET_TRANSFORMATION_INTERPOLATION_DELAY, display, ticks);
    }

    public static int displayPosRotInterpolationDuration(Display display) {
        return invoke(DISPLAY_GET_POS_ROT_INTERPOLATION_DURATION, display);
    }

    public static void setDisplayPosRotInterpolationDuration(Display display, int ticks) {
        invoke(DISPLAY_SET_POS_ROT_INTERPOLATION_DURATION, display, ticks);
    }

    public static float displayShadowRadius(Display display) {
        return invoke(DISPLAY_GET_SHADOW_RADIUS, display);
    }

    public static void setDisplayShadowRadius(Display display, float shadowRadius) {
        invoke(DISPLAY_SET_SHADOW_RADIUS, display, shadowRadius);
    }

    public static float displayShadowStrength(Display display) {
        return invoke(DISPLAY_GET_SHADOW_STRENGTH, display);
    }

    public static void setDisplayShadowStrength(Display display, float shadowStrength) {
        invoke(DISPLAY_SET_SHADOW_STRENGTH, display, shadowStrength);
    }

    public static float displayWidth(Display display) {
        return invoke(DISPLAY_GET_WIDTH, display);
    }

    public static void setDisplayWidth(Display display, float width) {
        invoke(DISPLAY_SET_WIDTH, display, width);
    }

    public static float displayHeight(Display display) {
        return invoke(DISPLAY_GET_HEIGHT, display);
    }

    public static void setDisplayHeight(Display display, float height) {
        invoke(DISPLAY_SET_HEIGHT, display, height);
    }

    public static int displayGlowColorOverride(Display display) {
        return invoke(DISPLAY_GET_GLOW_COLOR_OVERRIDE, display);
    }

    public static void setDisplayGlowColorOverride(Display display, int glowColorOverride) {
        invoke(DISPLAY_SET_GLOW_COLOR_OVERRIDE, display, glowColorOverride);
    }

    public static Transformation displayTransformation(Display display) {
        return invoke(DISPLAY_GET_TRANSFORMATION, null, display.getEntityData());
    }

    public static void setDisplayTransformation(Display display, Transformation transformation) {
        invoke(DISPLAY_SET_TRANSFORMATION, display, transformation);
    }

    public static byte textDisplayFlags(Display.TextDisplay textDisplay) {
        return invoke(TEXT_DISPLAY_GET_FLAGS, textDisplay);
    }

    public static void setTextDisplayFlags(Display.TextDisplay textDisplay, byte flags) {
        invoke(TEXT_DISPLAY_SET_FLAGS, textDisplay, flags);
    }

    public static Display.TextDisplay.Align textDisplayAlignment(Display.TextDisplay textDisplay) {
        return Display.TextDisplay.getAlign(textDisplayFlags(textDisplay));
    }

    public static void setTextDisplayAlignment(Display.TextDisplay textDisplay, Display.TextDisplay.Align align) {
        byte flags = textDisplayFlags(textDisplay);
        flags &= ~(Display.TextDisplay.FLAG_ALIGN_LEFT | Display.TextDisplay.FLAG_ALIGN_RIGHT);
        if (align == Display.TextDisplay.Align.LEFT) {
            flags |= Display.TextDisplay.FLAG_ALIGN_LEFT;
        } else if (align == Display.TextDisplay.Align.RIGHT) {
            flags |= Display.TextDisplay.FLAG_ALIGN_RIGHT;
        }
        setTextDisplayFlags(textDisplay, flags);
    }

    public static int textDisplayLineWidth(Display.TextDisplay textDisplay) {
        return invoke(TEXT_DISPLAY_GET_LINE_WIDTH, textDisplay);
    }

    public static void setTextDisplayLineWidth(Display.TextDisplay textDisplay, int lineWidth) {
        invoke(TEXT_DISPLAY_SET_LINE_WIDTH, textDisplay, lineWidth);
    }

    public static byte textDisplayOpacity(Display.TextDisplay textDisplay) {
        return invoke(TEXT_DISPLAY_GET_TEXT_OPACITY, textDisplay);
    }

    public static void setTextDisplayOpacity(Display.TextDisplay textDisplay, byte opacity) {
        invoke(TEXT_DISPLAY_SET_TEXT_OPACITY, textDisplay, opacity);
    }

    public static Component textDisplayText(Display.TextDisplay textDisplay) {
        return invoke(TEXT_DISPLAY_GET_TEXT, textDisplay);
    }

    public static void setTextDisplayText(Display.TextDisplay textDisplay, Component text) {
        invoke(TEXT_DISPLAY_SET_TEXT, textDisplay, text);
    }

    public static ItemDisplayContext itemDisplayTransform(Display.ItemDisplay itemDisplay) {
        return invoke(ITEM_DISPLAY_GET_ITEM_TRANSFORM, itemDisplay);
    }

    public static void setItemDisplayTransform(Display.ItemDisplay itemDisplay, ItemDisplayContext transform) {
        invoke(ITEM_DISPLAY_SET_ITEM_TRANSFORM, itemDisplay, transform);
    }

    public static boolean interactionResponse(Interaction interaction) {
        return invoke(INTERACTION_GET_RESPONSE, interaction);
    }

    public static float interactionWidth(Interaction interaction) {
        return invoke(INTERACTION_GET_WIDTH, interaction);
    }

    public static void setInteractionWidth(Interaction interaction, float width) {
        invoke(INTERACTION_SET_WIDTH, interaction, width);
    }

    public static float interactionHeight(Interaction interaction) {
        return invoke(INTERACTION_GET_HEIGHT, interaction);
    }

    public static void setInteractionHeight(Interaction interaction, float height) {
        invoke(INTERACTION_SET_HEIGHT, interaction, height);
    }

    public static void setInteractionResponse(Interaction interaction, boolean response) {
        invoke(INTERACTION_SET_RESPONSE, interaction, response);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Method method, Object target, Object... arguments) {
        try {
            return (T) method.invoke(target, arguments);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to access private Minecraft entity state via reflection.", exception);
        }
    }

    private static Method lookup(Class<?> owner, String name, Class<?> expectedReturnType, Class<?>... parameterTypes) {
        try {
            Method method = owner.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            if (expectedReturnType != void.class && !expectedReturnType.isAssignableFrom(method.getReturnType())) {
                throw new IllegalStateException("Unexpected return type for " + owner.getName() + "#" + name);
            }
            return method;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Missing expected Minecraft method " + owner.getName() + "#" + name, exception);
        }
    }
}
