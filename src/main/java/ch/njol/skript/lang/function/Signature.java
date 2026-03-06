package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Function signature: name, parameter types and optional return type.
 */
public class Signature<T> {

    final @Nullable String script;
    final String name;
    final Map<String, Parameter<?>> parameters;
    final boolean local;
    final @Nullable ClassInfo<T> returnType;
    final Class<?> returns;
    final boolean single;
    final Collection<Object> calls;
    @Nullable String originClassPath;

    static Class<?> getReturns(boolean single, Class<?> clazz) {
        return single ? clazz : clazz.arrayType();
    }

    public Signature(
            @Nullable String script,
            String name,
            Parameter<?>[] parameters,
            boolean local,
            @Nullable ClassInfo<T> returnType,
            boolean single
    ) {
        this(script, name, parameters, local, returnType, single, "");
    }

    public Signature(
            @Nullable String script,
            String name,
            Parameter<?>[] parameters,
            boolean local,
            @Nullable ClassInfo<T> returnType,
            boolean single,
            String originClassPath
    ) {
        this.script = script;
        this.name = name;
        this.parameters = initParameters(parameters);
        this.local = local;
        this.returnType = returnType;
        this.single = single;
        this.returns = returnType == null ? null : getReturns(single, returnType.getC());
        this.calls = Collections.newSetFromMap(new WeakHashMap<>());
        this.originClassPath = originClassPath;
    }

    public Signature(
            @Nullable String script,
            String name,
            Map<String, Parameter<?>> parameters,
            Class<T> returnType,
            boolean local
    ) {
        this.script = script;
        this.name = name;
        this.parameters = new LinkedHashMap<>(parameters);
        this.local = local;
        this.returns = returnType;
        if (returnType != null) {
            @SuppressWarnings("unchecked")
            ClassInfo<T> info = (ClassInfo<T>) ch.njol.skript.registrations.Classes.getSuperClassInfo(componentType(returnType));
            this.returnType = info;
            this.single = !returnType.isArray();
        } else {
            this.returnType = null;
            this.single = true;
        }
        this.calls = Collections.newSetFromMap(new WeakHashMap<>());
    }

    private static Map<String, Parameter<?>> initParameters(Parameter<?>[] params) {
        Map<String, Parameter<?>> map = new LinkedHashMap<>();
        if (params != null) {
            for (Parameter<?> parameter : params) {
                map.put(parameter.name(), parameter);
            }
        }
        return map;
    }

    public String getName() {
        return name;
    }

    public boolean isLocal() {
        return local;
    }

    public @Nullable String namespace() {
        return script;
    }

    public @Nullable ClassInfo<T> getReturnType() {
        return returnType;
    }

    public @Nullable Class<T> returnType() {
        @SuppressWarnings("unchecked")
        Class<T> cast = (Class<T>) returns;
        return cast;
    }

    public boolean isSingle() {
        return single;
    }

    public String getOriginClassPath() {
        return originClassPath;
    }

    public Parameter<?> getParameter(int index) {
        return getParameters()[index];
    }

    public Parameter<?> getParameter(String name) {
        return parameters.get(name);
    }

    public Parameter<?>[] getParameters() {
        return parameters.values().toArray(Parameter[]::new);
    }

    public Collection<Parameter<?>> parameters() {
        return Collections.unmodifiableCollection(parameters.values());
    }

    public int getMaxParameters() {
        return parameters.size();
    }

    public int getMinParameters() {
        List<Parameter<?>> list = new ArrayList<>(parameters.values());
        for (int i = list.size() - 1; i >= 0; i--) {
            if (!list.get(i).isOptional()) {
                return i + 1;
            }
        }
        return 0;
    }

    public void addCall(Object reference) {
        calls.add(reference);
    }

    public Collection<Object> calls() {
        return calls;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(script, name, local, single, returns);
        result = 31 * result + Arrays.hashCode(getParameters());
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Signature<?> other)) {
            return false;
        }
        return local == other.local
                && single == other.single
                && Objects.equals(script, other.script)
                && name.equals(other.name)
                && Objects.equals(returns, other.returns)
                && Arrays.equals(getParameters(), other.getParameters());
    }

    @Override
    public String toString() {
        String scope = local ? "local function " : "function ";
        return scope + name + "(" + parameterDescription() + ")" + returnDescription();
    }

    private String parameterDescription() {
        if (parameters.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Parameter<?> parameter : parameters.values()) {
            if (index++ > 0) {
                builder.append(", ");
            }
            builder.append(parameter.name())
                    .append(": ")
                    .append(componentType(parameter.type()).getSimpleName());
            if (!parameter.isSingle()) {
                builder.append("[]");
            }
            if (parameter.isOptional()) {
                builder.append("?");
            }
        }
        return builder.toString();
    }

    private String returnDescription() {
        if (returnType == null) {
            return "";
        }
        String type = returnType.getC().getSimpleName();
        return " :: " + (single ? type : type + "[]");
    }

    private static Class<?> componentType(Class<?> type) {
        return type.isArray() ? type.getComponentType() : type;
    }
}
