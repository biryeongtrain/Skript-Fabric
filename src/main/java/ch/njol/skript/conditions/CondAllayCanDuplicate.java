package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import java.lang.reflect.Method;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;

@Name("Allay Can Duplicate")
@Description("Checks to see if an allay is able to duplicate naturally.")
@Example("""
	if last spawned allay can duplicate:
		disallow last spawned to duplicate
	""")
@Since("2.11")
public class CondAllayCanDuplicate extends PropertyCondition<LivingEntity> {

    private static final Method CAN_DUPLICATE_METHOD = findCanDuplicateMethod();

    static {
        register(CondAllayCanDuplicate.class, PropertyType.CAN, "(duplicate|clone)", "livingentities");
    }

    @Override
    public boolean check(LivingEntity entity) {
        return entity instanceof Allay allay && canDuplicate(allay);
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.CAN;
    }

    @Override
    protected String getPropertyName() {
        return "duplicate";
    }

    private static boolean canDuplicate(Allay allay) {
        try {
            return (boolean) CAN_DUPLICATE_METHOD.invoke(allay);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read allay duplication state.", exception);
        }
    }

    private static Method findCanDuplicateMethod() {
        try {
            Method method = Allay.class.getDeclaredMethod("canDuplicate");
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access allay duplication state.", exception);
        }
    }
}
