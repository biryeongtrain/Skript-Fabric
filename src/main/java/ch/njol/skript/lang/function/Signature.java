package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Contract;
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
public class Signature<T> implements org.skriptlang.skript.common.function.Signature<T> {

    final @Nullable String script;
    final String name;
    final Map<String, Parameter<?>> parameters;
    final boolean local;
    final @Nullable ClassInfo<T> returnType;
    final Class<?> returns;
    final boolean single;
    final Collection<Object> calls;
    final @Nullable Contract contract;
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
        this(script, name, parameters, local, returnType, single, "", null);
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
        this(script, name, parameters, local, returnType, single, originClassPath, null);
    }

    public Signature(
            @Nullable String script,
            String name,
            Parameter<?>[] parameters,
            boolean local,
            @Nullable ClassInfo<T> returnType,
            boolean single,
            @Nullable Contract contract
    ) {
        this(script, name, parameters, local, returnType, single, "", contract);
    }

    public Signature(
            @Nullable String script,
            String name,
            Parameter<?>[] parameters,
            boolean local,
            @Nullable ClassInfo<T> returnType,
            boolean single,
            String originClassPath,
            @Nullable Contract contract
    ) {
        this.script = script;
        this.name = name;
        this.parameters = initParameters(parameters);
        this.local = local;
        this.returnType = returnType;
        this.single = single;
        this.returns = returnType == null ? null : getReturns(single, returnType.getC());
        this.calls = Collections.newSetFromMap(new WeakHashMap<>());
        this.contract = contract;
        this.originClassPath = originClassPath;
    }

    public Signature(
            @Nullable String script,
            String name,
            Map<String, Parameter<?>> parameters,
            Class<T> returnType,
            boolean local
    ) {
        this(script, name, parameters, returnType, local, null);
    }

    public Signature(
            @Nullable String script,
            String name,
            Map<String, Parameter<?>> parameters,
            Class<T> returnType,
            boolean local,
            @Nullable Contract contract
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
        this.contract = contract;
    }

    public Signature(
            @Nullable String script,
            String name,
            org.skriptlang.skript.common.function.Parameter<?>[] parameters,
            Class<T> returnType,
            boolean single,
            @Nullable Contract contract
    ) {
        this.script = script;
        this.name = name;
        this.parameters = initParameters(parameters);
        this.local = false;
        this.returns = returnType;
        if (returnType != null) {
            @SuppressWarnings("unchecked")
            ClassInfo<T> info = (ClassInfo<T>) ch.njol.skript.registrations.Classes.getSuperClassInfo(componentType(returnType));
            this.returnType = info;
        } else {
            this.returnType = null;
        }
        this.single = single;
        this.calls = Collections.newSetFromMap(new WeakHashMap<>());
        this.contract = contract;
    }

    private static Map<String, Parameter<?>> initParameters(org.skriptlang.skript.common.function.Parameter<?>[] params) {
        Map<String, Parameter<?>> map = new LinkedHashMap<>();
        if (params != null) {
            for (org.skriptlang.skript.common.function.Parameter<?> parameter : params) {
                map.put(parameter.name(), toOldParameter(parameter));
            }
        }
        return map;
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

    public Collection<Parameter<?>> legacyParameters() {
        return Collections.unmodifiableCollection(parameters.values());
    }

    @Override
    public org.skriptlang.skript.common.function.Parameters parameters() {
        LinkedHashMap<String, org.skriptlang.skript.common.function.Parameter<?>> mapped = new LinkedHashMap<>();
        for (Parameter<?> parameter : parameters.values()) {
            mapped.put(parameter.name(), toCommonParameter(parameter));
        }
        return new org.skriptlang.skript.common.function.Parameters(mapped);
    }

    @Override
    public @Nullable Contract contract() {
        return contract;
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

    @Override
    public void addCall(org.skriptlang.skript.common.function.FunctionReference<?> reference) {
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

    static Parameter<?> toOldParameter(org.skriptlang.skript.common.function.Parameter<?> parameter) {
        if (parameter == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        ClassInfo<Object> classInfo = (ClassInfo<Object>) ch.njol.skript.registrations.Classes.getSuperClassInfo(componentType(parameter.type()));
        Parameter.Modifier[] modifiers = parameter.modifiers().stream()
                .map(Signature::toLegacyModifier)
                .filter(java.util.Objects::nonNull)
                .toArray(Parameter.Modifier[]::new);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Parameter<?> legacy = new Parameter(parameter.name(), classInfo, !parameter.type().isArray(), null, modifiers);
        return legacy;
    }

    private static org.skriptlang.skript.common.function.Parameter<?> toCommonParameter(Parameter<?> parameter) {
        java.util.LinkedHashSet<org.skriptlang.skript.common.function.Parameter.Modifier> modifiers = new java.util.LinkedHashSet<>();
        if (parameter.hasModifier(Parameter.Modifier.OPTIONAL)) {
            modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL);
        }
        if (parameter.hasModifier(Parameter.Modifier.KEYED)) {
            modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED);
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        org.skriptlang.skript.common.function.ScriptParameter<?> common = new org.skriptlang.skript.common.function.ScriptParameter(
                parameter.name(),
                parameter.type(),
                modifiers,
                parameter.getDefaultExpression()
        );
        return common;
    }

    private static @Nullable Parameter.Modifier toLegacyModifier(
            org.skriptlang.skript.common.function.Parameter.Modifier modifier
    ) {
        if (modifier == org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL) {
            return Parameter.Modifier.OPTIONAL;
        }
        if (modifier == org.skriptlang.skript.common.function.Parameter.Modifier.KEYED) {
            return Parameter.Modifier.KEYED;
        }
        return null;
    }
}
