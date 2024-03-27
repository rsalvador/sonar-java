package org.sonar.samples.java.utils;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.sonar.plugins.java.api.semantic.Symbol.MethodSymbol;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

public class PrinterVisitor extends BaseTreeVisitor {

    private static final int INDENT_SPACES = 2;

    private final StringBuilder sb;
    private int indentLevel;

    public PrinterVisitor() {
        sb = new StringBuilder();
        indentLevel = 0;
    }

    public static String print(Tree tree) {
        PrinterVisitor pv = new PrinterVisitor();
        pv.scan(tree);
        return pv.sb.toString();
    }

    private StringBuilder indent() {
        for (int i = 0; i < INDENT_SPACES * indentLevel; i++)
            sb.append(' ');
        return sb;
    }

    @Override
    protected void scan(List<? extends Tree> trees) {
        if (!trees.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" : [\n");
            super.scan(trees);
            indent().append("]\n");
        }
    }

    @Override
    protected void scan(@Nullable Tree tree) {
        if (tree != null && tree.getClass().getInterfaces() != null && tree.getClass().getInterfaces().length > 0) {
            indent();
            appendNode(sb, tree);
            sb.append('\n');
        }
        indentLevel++;
        super.scan(tree);
        indentLevel--;
    }

    private static void appendNode(StringBuilder sb, Tree tree) {
        String nodeName = tree.getClass().getInterfaces()[0].getSimpleName();
        sb.append(nodeName).append(" [").append(Integer.toHexString(tree.hashCode())).append(']');
        String name = getName(tree);
        if (name != null)
            sb.append(" (").append(name).append(')');
    }

    public static String printNode(Tree tree) {
        StringBuilder sb = new StringBuilder();
        appendNode(sb, tree);
        return sb.toString();
    }

    public static String printList(List<IdentifierTree> usages) {
        return usages.stream().map(Object::hashCode).map(Integer::toHexString).collect(Collectors.toList()).toString();
    }

    public static String printSymbolList(List<MethodSymbol> list) {
        return list.stream().map(Object::hashCode).map(Integer::toHexString).collect(Collectors.toList()).toString();
    }

    private static String getName(Tree tree) {
        if (tree instanceof VariableTree)
            return ((VariableTree) tree).simpleName().name();
        if (tree instanceof IdentifierTree)
            return ((IdentifierTree) tree).name();
        return null;
    }
}