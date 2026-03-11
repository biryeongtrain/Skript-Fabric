package org.skriptlang.skript.fabric.compat;

import com.mojang.math.Transformation;
import kim.biryeong.skriptFabric.mixin.DisplayAccessor;
import kim.biryeong.skriptFabric.mixin.InteractionAccessor;
import kim.biryeong.skriptFabric.mixin.ItemDisplayAccessor;
import kim.biryeong.skriptFabric.mixin.TextDisplayAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.item.ItemDisplayContext;

public final class PrivateEntityAccess {

    private PrivateEntityAccess() {
    }

    public static Display.BillboardConstraints displayBillboardConstraints(Display display) {
        return displayAccessor(display).skript$invokeGetBillboardConstraints();
    }

    public static void setDisplayBillboardConstraints(Display display, Display.BillboardConstraints constraints) {
        displayAccessor(display).skript$invokeSetBillboardConstraints(constraints);
    }

    public static Brightness displayBrightnessOverride(Display display) {
        return displayAccessor(display).skript$invokeGetBrightnessOverride();
    }

    public static void setDisplayBrightnessOverride(Display display, Brightness brightness) {
        displayAccessor(display).skript$invokeSetBrightnessOverride(brightness);
    }

    public static float displayViewRange(Display display) {
        return displayAccessor(display).skript$invokeGetViewRange();
    }

    public static void setDisplayViewRange(Display display, float viewRange) {
        displayAccessor(display).skript$invokeSetViewRange(viewRange);
    }

    public static int displayTransformationInterpolationDuration(Display display) {
        return displayAccessor(display).skript$invokeGetTransformationInterpolationDuration();
    }

    public static void setDisplayTransformationInterpolationDuration(Display display, int ticks) {
        displayAccessor(display).skript$invokeSetTransformationInterpolationDuration(ticks);
    }

    public static int displayTransformationInterpolationDelay(Display display) {
        return displayAccessor(display).skript$invokeGetTransformationInterpolationDelay();
    }

    public static void setDisplayTransformationInterpolationDelay(Display display, int ticks) {
        displayAccessor(display).skript$invokeSetTransformationInterpolationDelay(ticks);
    }

    public static int displayPosRotInterpolationDuration(Display display) {
        return displayAccessor(display).skript$invokeGetPosRotInterpolationDuration();
    }

    public static void setDisplayPosRotInterpolationDuration(Display display, int ticks) {
        displayAccessor(display).skript$invokeSetPosRotInterpolationDuration(ticks);
    }

    public static float displayShadowRadius(Display display) {
        return displayAccessor(display).skript$invokeGetShadowRadius();
    }

    public static void setDisplayShadowRadius(Display display, float shadowRadius) {
        displayAccessor(display).skript$invokeSetShadowRadius(shadowRadius);
    }

    public static float displayShadowStrength(Display display) {
        return displayAccessor(display).skript$invokeGetShadowStrength();
    }

    public static void setDisplayShadowStrength(Display display, float shadowStrength) {
        displayAccessor(display).skript$invokeSetShadowStrength(shadowStrength);
    }

    public static float displayWidth(Display display) {
        return displayAccessor(display).skript$invokeGetWidth();
    }

    public static void setDisplayWidth(Display display, float width) {
        displayAccessor(display).skript$invokeSetWidth(width);
    }

    public static float displayHeight(Display display) {
        return displayAccessor(display).skript$invokeGetHeight();
    }

    public static void setDisplayHeight(Display display, float height) {
        displayAccessor(display).skript$invokeSetHeight(height);
    }

    public static int displayGlowColorOverride(Display display) {
        return displayAccessor(display).skript$invokeGetGlowColorOverride();
    }

    public static void setDisplayGlowColorOverride(Display display, int glowColorOverride) {
        displayAccessor(display).skript$invokeSetGlowColorOverride(glowColorOverride);
    }

    public static Transformation displayTransformation(Display display) {
        SynchedEntityData data = display.getEntityData();
        return DisplayAccessor.skript$invokeCreateTransformation(data);
    }

    public static void setDisplayTransformation(Display display, Transformation transformation) {
        displayAccessor(display).skript$invokeSetTransformation(transformation);
    }

    public static byte textDisplayFlags(Display.TextDisplay textDisplay) {
        return textDisplayAccessor(textDisplay).skript$invokeGetFlags();
    }

    public static void setTextDisplayFlags(Display.TextDisplay textDisplay, byte flags) {
        textDisplayAccessor(textDisplay).skript$invokeSetFlags(flags);
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
        return textDisplayAccessor(textDisplay).skript$invokeGetLineWidth();
    }

    public static void setTextDisplayLineWidth(Display.TextDisplay textDisplay, int lineWidth) {
        textDisplayAccessor(textDisplay).skript$invokeSetLineWidth(lineWidth);
    }

    public static byte textDisplayOpacity(Display.TextDisplay textDisplay) {
        return textDisplayAccessor(textDisplay).skript$invokeGetTextOpacity();
    }

    public static void setTextDisplayOpacity(Display.TextDisplay textDisplay, byte opacity) {
        textDisplayAccessor(textDisplay).skript$invokeSetTextOpacity(opacity);
    }

    public static Component textDisplayText(Display.TextDisplay textDisplay) {
        return textDisplayAccessor(textDisplay).skript$invokeGetText();
    }

    public static void setTextDisplayText(Display.TextDisplay textDisplay, Component text) {
        textDisplayAccessor(textDisplay).skript$invokeSetText(text);
    }

    public static ItemDisplayContext itemDisplayTransform(Display.ItemDisplay itemDisplay) {
        return itemDisplayAccessor(itemDisplay).skript$invokeGetItemTransform();
    }

    public static void setItemDisplayTransform(Display.ItemDisplay itemDisplay, ItemDisplayContext transform) {
        itemDisplayAccessor(itemDisplay).skript$invokeSetItemTransform(transform);
    }

    public static boolean interactionResponse(Interaction interaction) {
        return interactionAccessor(interaction).skript$invokeGetResponse();
    }

    public static float interactionWidth(Interaction interaction) {
        return interactionAccessor(interaction).skript$invokeGetWidth();
    }

    public static void setInteractionWidth(Interaction interaction, float width) {
        interactionAccessor(interaction).skript$invokeSetWidth(width);
    }

    public static float interactionHeight(Interaction interaction) {
        return interactionAccessor(interaction).skript$invokeGetHeight();
    }

    public static void setInteractionHeight(Interaction interaction, float height) {
        interactionAccessor(interaction).skript$invokeSetHeight(height);
    }

    public static void setInteractionResponse(Interaction interaction, boolean response) {
        interactionAccessor(interaction).skript$invokeSetResponse(response);
    }

    private static DisplayAccessor displayAccessor(Display display) {
        return (DisplayAccessor) display;
    }

    private static TextDisplayAccessor textDisplayAccessor(Display.TextDisplay textDisplay) {
        return (TextDisplayAccessor) textDisplay;
    }

    private static ItemDisplayAccessor itemDisplayAccessor(Display.ItemDisplay itemDisplay) {
        return (ItemDisplayAccessor) itemDisplay;
    }

    private static InteractionAccessor interactionAccessor(Interaction interaction) {
        return (InteractionAccessor) interaction;
    }
}
