package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.jetbrains.annotations.Nullable;

/**
 * Upstream-style storage for direct and list-backed variable values.
 */
final class VariablesMap {

    static final Comparator<String> VARIABLE_NAME_COMPARATOR = (first, second) -> {
        if (first == null) {
            return second == null ? 0 : -1;
        }
        if (second == null) {
            return 1;
        }

        int firstIndex = 0;
        int secondIndex = 0;
        boolean lastNumberNegative = false;
        boolean afterDecimalPoint = false;
        while (firstIndex < first.length() && secondIndex < second.length()) {
            char firstChar = first.charAt(firstIndex);
            char secondChar = second.charAt(secondIndex);

            if (Character.isDigit(firstChar) && Character.isDigit(secondChar)) {
                int firstEnd = findLastDigit(first, firstIndex);
                int secondEnd = findLastDigit(second, secondIndex);
                int firstLeadingZeros = 0;
                int secondLeadingZeros = 0;

                if (!afterDecimalPoint) {
                    while (firstIndex < firstEnd - 1 && first.charAt(firstIndex) == '0') {
                        firstIndex++;
                        firstLeadingZeros++;
                    }
                    while (secondIndex < secondEnd - 1 && second.charAt(secondIndex) == '0') {
                        secondIndex++;
                        secondLeadingZeros++;
                    }
                }

                boolean previousNegative = lastNumberNegative;
                lastNumberNegative = firstIndex - firstLeadingZeros > 0
                        && first.charAt(firstIndex - firstLeadingZeros - 1) == '-';
                int sign = (lastNumberNegative || previousNegative) ? -1 : 1;

                if (!afterDecimalPoint && firstEnd - firstIndex != secondEnd - secondIndex) {
                    return ((firstEnd - firstIndex) - (secondEnd - secondIndex)) * sign;
                }

                while (firstIndex < firstEnd && secondIndex < secondEnd) {
                    char firstDigit = first.charAt(firstIndex);
                    char secondDigit = second.charAt(secondIndex);
                    if (firstDigit != secondDigit) {
                        return (firstDigit - secondDigit) * sign;
                    }
                    firstIndex++;
                    secondIndex++;
                }

                if (afterDecimalPoint && firstEnd - firstIndex != secondEnd - secondIndex) {
                    return ((firstEnd - firstIndex) - (secondEnd - secondIndex)) * sign;
                }
                if (firstLeadingZeros != secondLeadingZeros) {
                    return (firstLeadingZeros - secondLeadingZeros) * sign;
                }

                afterDecimalPoint = true;
                continue;
            }

            if (firstChar != secondChar) {
                return firstChar - secondChar;
            }
            if (firstChar != '.') {
                lastNumberNegative = false;
                afterDecimalPoint = false;
            }
            firstIndex++;
            secondIndex++;
        }

        if (firstIndex < first.length()) {
            return lastNumberNegative ? -1 : 1;
        }
        if (secondIndex < second.length()) {
            return lastNumberNegative ? 1 : -1;
        }
        return 0;
    };

    final HashMap<String, Object> hashMap = new HashMap<>();
    final TreeMap<String, Object> treeMap = new TreeMap<>();

    @SuppressWarnings("unchecked")
    @Nullable Object getVariable(String name) {
        if (!name.endsWith("*")) {
            return hashMap.get(name);
        }

        String[] split = Variables.splitVariableName(name);
        Map<String, Object> parent = treeMap;
        for (int i = 0; i < split.length; i++) {
            String part = split[i];
            if (part.equals("*")) {
                return parent;
            }

            Object child = parent.get(part);
            if (child == null) {
                return null;
            }
            if (child instanceof Map<?, ?> map) {
                parent = (Map<String, Object>) map;
                continue;
            }
            return null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    void setVariable(String name, @Nullable Object value) {
        if (!name.endsWith("*")) {
            if (value == null) {
                hashMap.remove(name);
            } else {
                hashMap.put(name, value);
            }
        }

        String[] split = Variables.splitVariableName(name);
        TreeMap<String, Object> parent = treeMap;
        for (int i = 0; i < split.length; i++) {
            String childNodeName = split[i];
            Object childNode = parent.get(childNodeName);

            if (childNode == null) {
                if (i == split.length - 1) {
                    if (value != null) {
                        parent.put(childNodeName, value);
                    }
                    break;
                }
                if (value == null) {
                    break;
                }
                childNode = new TreeMap<>(VARIABLE_NAME_COMPARATOR);
                parent.put(childNodeName, childNode);
                parent = (TreeMap<String, Object>) childNode;
                continue;
            }

            if (childNode instanceof TreeMap<?, ?> childNodeMap) {
                TreeMap<String, Object> castChildNodeMap = (TreeMap<String, Object>) childNodeMap;
                if (i == split.length - 1) {
                    if (value == null) {
                        castChildNodeMap.remove(null);
                    } else {
                        castChildNodeMap.put(null, value);
                    }
                    break;
                }
                if (i == split.length - 2 && split[i + 1].equals("*")) {
                    assert value == null;
                    deleteFromHashMap(joinPrefix(split, i + 1), castChildNodeMap);
                    Object currentChildValue = castChildNodeMap.get(null);
                    if (currentChildValue == null) {
                        parent.remove(childNodeName);
                    } else {
                        parent.put(childNodeName, currentChildValue);
                    }
                    break;
                }
                parent = castChildNodeMap;
                continue;
            }

            if (i == split.length - 1) {
                if (value == null) {
                    parent.remove(childNodeName);
                } else {
                    parent.put(childNodeName, value);
                }
                break;
            }
            if (value == null) {
                break;
            }

            TreeMap<String, Object> newChildNodeMap = new TreeMap<>(VARIABLE_NAME_COMPARATOR);
            newChildNodeMap.put(null, childNode);
            parent.put(childNodeName, newChildNodeMap);
            parent = newChildNodeMap;
        }
    }

    VariablesMap copy() {
        VariablesMap copy = new VariablesMap();
        copy.hashMap.putAll(hashMap);
        copy.treeMap.putAll(copyTreeMap(treeMap));
        return copy;
    }

    void clear() {
        hashMap.clear();
        treeMap.clear();
    }

    @SuppressWarnings("unchecked")
    private void deleteFromHashMap(String parent, TreeMap<String, Object> current) {
        for (Entry<String, Object> entry : current.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String childName = parent + Variable.SEPARATOR + entry.getKey();
            hashMap.remove(childName);
            if (entry.getValue() instanceof TreeMap<?, ?> treeMapValue) {
                deleteFromHashMap(childName, (TreeMap<String, Object>) treeMapValue);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static TreeMap<String, Object> copyTreeMap(TreeMap<String, Object> original) {
        TreeMap<String, Object> copy = new TreeMap<>(VARIABLE_NAME_COMPARATOR);
        for (Entry<String, Object> child : original.entrySet()) {
            Object value = child.getValue();
            if (value instanceof TreeMap<?, ?> treeMapValue) {
                value = copyTreeMap((TreeMap<String, Object>) treeMapValue);
            }
            copy.put(child.getKey(), value);
        }
        return copy;
    }

    private static int findLastDigit(String input, int start) {
        int index = start;
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            index++;
        }
        return index;
    }

    private static String joinPrefix(String[] split, int endExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < endExclusive; i++) {
            if (i > 0) {
                builder.append(Variable.SEPARATOR);
            }
            builder.append(split[i]);
        }
        return builder.toString();
    }
}
