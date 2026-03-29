package ch.njol.skript.conditions;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.util.Objects;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondCompare extends Condition {

    // Pattern indices:
    // 0: %objects% (is|are) (greater|more|higher|bigger|larger) than %objects%
    // 1: %objects% > %objects%
    // 2: %objects% (is|are) (greater|more|higher|bigger|larger) than or equal to %objects%
    // 3: %objects% >= %objects%
    // 4: %objects% (is|are) (less|smaller|lower) than %objects%
    // 5: %objects% < %objects%
    // 6: %objects% (is|are) (less|smaller|lower) than or equal to %objects%
    // 7: %objects% <= %objects%
    // 8: %objects% (is|are) %objects%
    // 9: %objects% (isn't|is not|aren't|are not) %objects%
    // 10: %objects% is between %objects% and %objects%
    // 11: %objects% is not between %objects% and %objects%

    private Expression<?> left;
    private Expression<?> right;
    private @Nullable Expression<?> third;
    private Relation relation;
    private boolean isBetween;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Class<?> leftReturnType = expressions[0].getReturnType();
        if (leftReturnType != Object.class && net.minecraft.world.entity.Entity.class.isAssignableFrom(leftReturnType)) {
            return false;
        }
        left = expressions[0];
        right = expressions[1];

        switch (matchedPattern) {
            case 0, 1 -> {
                relation = Relation.GREATER;
                if (expressions.length != 2) return false;
            }
            case 2, 3 -> {
                relation = Relation.GREATER_OR_EQUAL;
                if (expressions.length != 2) return false;
            }
            case 4, 5 -> {
                relation = Relation.SMALLER;
                if (expressions.length != 2) return false;
            }
            case 6, 7 -> {
                relation = Relation.SMALLER_OR_EQUAL;
                if (expressions.length != 2) return false;
            }
            case 8 -> {
                relation = Relation.EQUAL;
                if (expressions.length != 2) return false;
            }
            case 9 -> {
                relation = Relation.NOT_EQUAL;
                if (expressions.length != 2) return false;
            }
            case 10 -> {
                relation = Relation.EQUAL;
                isBetween = true;
                if (expressions.length != 3) return false;
                third = expressions[2];
            }
            case 11 -> {
                relation = Relation.EQUAL;
                isBetween = true;
                setNegated(true);
                if (expressions.length != 3) return false;
                third = expressions[2];
            }
            default -> {
                return false;
            }
        }

        if (matchedPattern == 9 || matchedPattern == 11) {
            setNegated(true);
        }

        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        Object[] leftValues = left.getAll(event);
        Object[] rightValues = right.getAll(event);

        if (isBetween) {
            Object[] thirdValues = third != null ? third.getAll(event) : new Object[0];
            boolean result = false;
            for (Object leftValue : leftValues) {
                for (Object rightValue : rightValues) {
                    for (Object thirdValue : thirdValues) {
                        Relation lowerBound = compare(leftValue, rightValue);
                        Relation upperBound = compare(leftValue, thirdValue);
                        if (Relation.GREATER_OR_EQUAL.isImpliedBy(lowerBound)
                                && Relation.SMALLER_OR_EQUAL.isImpliedBy(upperBound)) {
                            result = true;
                            break;
                        }
                    }
                    if (result) break;
                }
                if (result) break;
            }
            return isNegated() ? !result : result;
        }

        boolean matched = false;
        for (Object leftValue : leftValues) {
            for (Object rightValue : rightValues) {
                Relation cmp = compare(leftValue, rightValue);
                if (relation.isImpliedBy(cmp)) {
                    matched = true;
                    break;
                }
            }
            if (matched) break;
        }
        return matched;
    }

    private Relation compare(@Nullable Object leftValue, @Nullable Object rightValue) {
        if (leftValue instanceof Number leftNumber && rightValue instanceof Number rightNumber) {
            int cmp = Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
            return Relation.get(cmp);
        }
        if (leftValue instanceof Number leftNumber && rightValue instanceof String rightString) {
            return Relation.get(matchesNumericAlias(leftNumber, rightString));
        }
        if (leftValue instanceof String leftString && rightValue instanceof Number rightNumber) {
            return Relation.get(matchesNumericAlias(rightNumber, leftString));
        }
        if (leftValue instanceof ItemStack leftItemStack) {
            return Relation.get(matchesItemStackAlias(leftItemStack, rightValue));
        }
        if (rightValue instanceof ItemStack rightItemStack) {
            return Relation.get(matchesItemStackAlias(rightItemStack, leftValue));
        }
        if (leftValue instanceof FabricItemType leftItemType) {
            return Relation.get(matchesItemTypeAlias(leftItemType, rightValue));
        }
        if (rightValue instanceof FabricItemType rightItemType) {
            return Relation.get(matchesItemTypeAlias(rightItemType, leftValue));
        }
        if (leftValue instanceof Display.TextDisplay.Align leftAlign && rightValue instanceof String rightString) {
            return Relation.get(matchesAlignAlias(leftAlign, rightString));
        }
        if (leftValue instanceof String leftString && rightValue instanceof Display.TextDisplay.Align rightAlign) {
            return Relation.get(matchesAlignAlias(rightAlign, leftString));
        }
        if (leftValue instanceof StringRepresentable leftRepresentable) {
            return Relation.get(matchesRepresentableAlias(leftRepresentable, rightValue));
        }
        if (rightValue instanceof StringRepresentable rightRepresentable) {
            return Relation.get(matchesRepresentableAlias(rightRepresentable, leftValue));
        }
        if (leftValue instanceof Enum<?> leftEnum) {
            return Relation.get(matchesEnumAlias(leftEnum, rightValue));
        }
        if (rightValue instanceof Enum<?> rightEnum) {
            return Relation.get(matchesEnumAlias(rightEnum, leftValue));
        }
        if (leftValue instanceof SkriptPotionEffect leftPotionEffect) {
            return Relation.get(matchesPotionEffectAlias(leftPotionEffect, rightValue));
        }
        if (rightValue instanceof SkriptPotionEffect rightPotionEffect) {
            return Relation.get(matchesPotionEffectAlias(rightPotionEffect, leftValue));
        }
        if (leftValue instanceof Timespan leftTimespan && rightValue instanceof String rightString) {
            return Relation.get(matchesTimespanAlias(leftTimespan, rightString));
        }
        if (leftValue instanceof String leftString && rightValue instanceof Timespan rightTimespan) {
            return Relation.get(matchesTimespanAlias(rightTimespan, leftString));
        }
        if (leftValue instanceof GameProfile leftProfile && rightValue instanceof String rightString) {
            return Relation.get(matchesProfileAlias(leftProfile, rightString));
        }
        if (leftValue instanceof String leftString && rightValue instanceof GameProfile rightProfile) {
            return Relation.get(matchesProfileAlias(rightProfile, leftString));
        }
        if (leftValue instanceof Vec3 leftVector && rightValue instanceof String rightString) {
            return Relation.get(matchesVectorAlias(leftVector, rightString));
        }
        if (leftValue instanceof String leftString && rightValue instanceof Vec3 rightVector) {
            return Relation.get(matchesVectorAlias(rightVector, leftString));
        }
        if (leftValue instanceof Quaternionf leftQuaternion && rightValue instanceof String rightString) {
            return Relation.get(matchesQuaternionAlias(leftQuaternion, rightString));
        }
        if (leftValue instanceof String leftString && rightValue instanceof Quaternionf rightQuaternion) {
            return Relation.get(matchesQuaternionAlias(rightQuaternion, leftString));
        }
        return Comparators.compare(leftValue, rightValue);
    }

    private boolean matchesAlignAlias(Display.TextDisplay.Align align, String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        return switch (align) {
            case LEFT -> normalized.equals("left") || normalized.equals("left aligned");
            case RIGHT -> normalized.equals("right") || normalized.equals("right aligned");
            case CENTER -> normalized.equals("center")
                    || normalized.equals("centered")
                    || normalized.equals("centre")
                    || normalized.equals("centred")
                    || normalized.equals("center aligned")
                    || normalized.equals("centre aligned")
                    || normalized.equals("centered aligned")
                    || normalized.equals("centred aligned");
        };
    }

    private boolean matchesNumericAlias(Number number, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            String normalized = value.trim();
            if (number instanceof Float || number instanceof Double) {
                return Float.compare(number.floatValue(), Float.parseFloat(normalized)) == 0;
            }
            return Long.compare(number.longValue(), Long.parseLong(normalized)) == 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private boolean matchesRepresentableAlias(StringRepresentable representable, @Nullable Object value) {
        String normalized = normalize(renderComparableValue(value));
        if (normalized.isEmpty()) {
            return false;
        }
        if (normalize(representable.getSerializedName()).equals(normalized)) {
            return true;
        }
        if (representable instanceof Enum<?> enumValue) {
            return normalize(enumValue.name()).equals(normalized);
        }
        return false;
    }

    private boolean matchesEnumAlias(Enum<?> enumValue, @Nullable Object value) {
        String normalized = normalize(renderComparableValue(value));
        return !normalized.isEmpty()
                && (normalize(enumValue.name()).equals(normalized) || normalize(enumValue.toString()).equals(normalized));
    }

    private boolean matchesItemStackAlias(ItemStack stack, @Nullable Object value) {
        if (value == null || stack.isEmpty()) {
            return false;
        }
        if (value instanceof ItemStack otherStack) {
            return stack.getCount() == otherStack.getCount() && ItemStack.isSameItemSameComponents(stack, otherStack);
        }
        FabricItemType parsed = parseItemType(value);
        return parsed != null && parsed.matches(stack);
    }

    private boolean matchesItemTypeAlias(FabricItemType itemType, @Nullable Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof ItemStack stack) {
            return itemType.matches(stack);
        }
        FabricItemType parsed = parseItemType(value);
        return parsed != null && itemTypesEqual(itemType, parsed);
    }

    private @Nullable FabricItemType parseItemType(@Nullable Object value) {
        if (value instanceof FabricItemType itemType) {
            return itemType;
        }
        if (value == null) {
            return null;
        }
        return Classes.parse(String.valueOf(value), FabricItemType.class, ParseContext.DEFAULT);
    }

    private boolean itemTypesEqual(FabricItemType left, FabricItemType right) {
        return left.item() == right.item()
                && left.amount() == right.amount()
                && Objects.equals(left.name(), right.name())
                && Objects.equals(left.equippable(), right.equippable());
    }

    private boolean matchesPotionEffectAlias(SkriptPotionEffect potionEffect, @Nullable Object value) {
        if (value == null) {
            return false;
        }
        SkriptPotionEffect parsedEffect = PotionEffectSupport.parsePotionEffect(value);
        if (parsedEffect != null && parsedEffect.type().value() == potionEffect.type().value()) {
            return true;
        }
        String normalized = normalize(renderComparableValue(value));
        return !normalized.isEmpty() && normalize(potionEffect.toString()).equals(normalized);
    }

    private boolean matchesTimespanAlias(Timespan timespan, String value) {
        Timespan parsed = Classes.parse(value, Timespan.class, ParseContext.DEFAULT);
        return parsed != null && parsed.equals(timespan);
    }

    private boolean matchesProfileAlias(GameProfile profile, String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return false;
        }
        if (normalize(profile.name()).equals(normalized)) {
            return true;
        }
        return profile.id() != null && normalize(profile.id().toString()).equals(normalized);
    }

    private boolean matchesVectorAlias(Vec3 vector, String value) {
        Vec3 parsed = Classes.parse(value, Vec3.class, ParseContext.DEFAULT);
        return parsed != null
                && Double.compare(vector.x, parsed.x) == 0
                && Double.compare(vector.y, parsed.y) == 0
                && Double.compare(vector.z, parsed.z) == 0;
    }

    private boolean matchesQuaternionAlias(Quaternionf quaternion, String value) {
        Quaternionf parsed = Classes.parse(value, Quaternionf.class, ParseContext.DEFAULT);
        return parsed != null
                && Float.compare(quaternion.x, parsed.x) == 0
                && Float.compare(quaternion.y, parsed.y) == 0
                && Float.compare(quaternion.z, parsed.z) == 0
                && Float.compare(quaternion.w, parsed.w) == 0;
    }

    private String normalize(@Nullable String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase().replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ");
    }

    private @Nullable String renderComparableValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof GameProfile profile) {
            return profile.name();
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return String.valueOf(value);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (isBetween) {
            String prefix = isNegated() ? " is not between " : " is between ";
            return left.toString(event, debug) + prefix + right.toString(event, debug)
                    + " and " + (third != null ? third.toString(event, debug) : "?");
        }
        String operator = switch (relation) {
            case GREATER -> " is greater than ";
            case GREATER_OR_EQUAL -> " is greater than or equal to ";
            case SMALLER -> " is less than ";
            case SMALLER_OR_EQUAL -> " is less than or equal to ";
            case NOT_EQUAL -> " is not ";
            default -> " is ";
        };
        return left.toString(event, debug) + operator + right.toString(event, debug);
    }
}
