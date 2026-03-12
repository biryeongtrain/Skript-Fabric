package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.NodeNavigator;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprNode extends PropertyExpression<NodeNavigator, Node> implements ReflectionExperimentSyntax {

    private static final String[] PATTERNS = {
            "[the] node %string% (of|in) %node%",
            "%node%'[s] node %string%",
            "[the] node %string% (of|in) %config%",
            "%config%'[s] node %string%",
            "[the] nodes (of|in) %nodes%",
            "%node%'[s] nodes",
            "[the] nodes (of|in) %configs%",
            "%config%'[s] nodes"
    };

    static {
        Skript.registerExpression(ExprNode.class, Node.class, PATTERNS);
    }

    private boolean isPath;
    private Expression<String> pathExpression;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {
        isPath = pattern < 4;
        switch (pattern) {
            case 0, 2 -> {
                pathExpression = (Expression<String>) expressions[0];
                setExpr((Expression<? extends NodeNavigator>) expressions[1]);
            }
            case 1, 3 -> {
                pathExpression = (Expression<String>) expressions[1];
                setExpr((Expression<? extends NodeNavigator>) expressions[0]);
            }
            default -> setExpr((Expression<? extends NodeNavigator>) expressions[0]);
        }
        return true;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return null;
    }

    @Override
    protected Node[] get(SkriptEvent event, NodeNavigator[] source) {
        if (source.length == 0) {
            return new Node[0];
        }
        if (isPath) {
            String path = pathExpression.getSingle(event);
            NodeNavigator navigator = source[0];
            if (navigator == null) {
                return new Node[0];
            }
            if (path == null || path.isBlank()) {
                return new Node[]{navigator.getCurrentNode()};
            }
            Node node = navigator.getNodeAt(path);
            return node == null ? new Node[0] : new Node[]{node};
        }

        Set<Node> nodes = new LinkedHashSet<>();
        for (NodeNavigator navigator : source) {
            if (navigator == null) {
                continue;
            }
            Iterator<Node> iterator = navigator instanceof Config config
                    ? config.getMainNode().iterator()
                    : navigator.iterator();
            while (iterator.hasNext()) {
                nodes.add(iterator.next());
            }
        }
        return nodes.toArray(Node[]::new);
    }

    @Override
    public @Nullable Iterator<? extends Node> iterator(SkriptEvent event) {
        if (isPath) {
            return super.iterator(event);
        }
        NodeNavigator navigator = getExpr().getSingle(event);
        if (navigator instanceof Config config) {
            return config.getMainNode().iterator();
        }
        if (navigator instanceof SectionNode sectionNode) {
            return sectionNode.iterator();
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return isPath;
    }

    @Override
    public Class<? extends Node> getReturnType() {
        return Node.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Node>[] possibleReturnTypes() {
        return new Class[]{Node.class, EntryNode.class};
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (isPath) {
            return "the node " + pathExpression.toString(event, debug) + " of " + getExpr().toString(event, debug);
        }
        return "the nodes of " + getExpr().toString(event, debug);
    }
}
