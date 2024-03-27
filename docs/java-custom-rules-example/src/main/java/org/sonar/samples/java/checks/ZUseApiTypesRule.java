package org.sonar.samples.java.checks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Symbol.MethodSymbol;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.samples.java.utils.PrinterVisitor;

@Rule(key = "ZUseApiTypes")
public class ZUseApiTypesRule extends BaseTreeVisitor implements JavaFileScanner {

    private JavaFileScannerContext context;

    @Override
    public void scanFile(JavaFileScannerContext context) {
        v("--------> scanFile: " + context.getInputFile());
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitMethod(MethodTree tree) {
        v("------> visiting method: " + tree.simpleName().name());
        v(PrinterVisitor.print(tree));
        super.visitMethod(tree);
    }

    @Override
    public void visitVariable(VariableTree var) {
        v("----> visiting variable: " + var.simpleName().name());
        v(PrinterVisitor.print(var));
        visitVariable0(var);
        super.visitVariable(var);
    }

    private void visitVariable0(VariableTree var) {
        Symbol varSymbol = var.symbol();
        Type varType = varSymbol.type();

        // if a class type collect its inheritance hierarchy
        if (!varType.isClass() || varType.isPrimitiveWrapper())
            return;
        if (varType.fullyQualifiedName().startsWith("java."))
            return;

        TypeSymbol typeSymbol = varType.symbol();
        List<Type> interfaces = getInterfaceHierarchy(typeSymbol);
        if (interfaces.size() == 0)
            return; // doesn't extend any interface

        // find all usages of the variable
        List<IdentifierTree> usages = varSymbol.usages();
        if (usages.size() == 0)
            return; // ignore unused variables

        v("--> usages: " + PrinterVisitor.printList(usages));
        // usages can be:
        // - method call
        // - field access
        // - variable assignment (or similar, e.g. returned, passed, ...)

        // list of candidate types to replace the variable with
        List<Type> candidateTypes = new ArrayList<>(interfaces);
        v("--> initial candidates: " + PrinterVisitor.printList(usages));
        for (IdentifierTree usage : usages) {
            // for each usage remove types that are not compatible with the usage
            Iterator<Type> iterator = candidateTypes.iterator();
            while (iterator.hasNext()) {
                Type type = iterator.next();
                if (!isCompatibleWithUsage(type, usage))
                    iterator.remove();
            }
        }

        if (candidateTypes.size() > 0) {
            v("--> after candidates: " + PrinterVisitor.printList(usages));
            // varType can be replaced by an interfaceType
            Type replaceType = candidateTypes.get(candidateTypes.size() - 1);
            String issue = String.format(
                    "Declare \"%s\" as \"%s\" instead of \"%s\"",
                    var.simpleName().name(), replaceType.name(), varType.name());
            context.reportIssue(this, var, issue);
            System.out.println("=====> ISSUE: " + issue);
        }
    }

    // UTILS

    private boolean isCompatibleWithUsage(Type type, IdentifierTree usage) {
        Tree parent = usage.parent();
        if (parent.parent().is(Tree.Kind.METHOD_INVOCATION)) {
            return isCompatibleWith(type, (MethodInvocationTree) parent.parent());
        }
        // throw new RuntimeException("TODO: " + parent);
        return false;
    }

    private boolean isCompatibleWith(Type type, MethodInvocationTree methodInvocation) {
        // compatible if type or any of its super types declares that method
        MethodSymbol methodSymbol = methodInvocation.methodSymbol();
        List<MethodSymbol> overriddenSymbols = methodSymbol.overriddenSymbols();
        TypeSymbol typeSymbol = type.symbol();
        for (MethodSymbol overridenMethodSymbol : overriddenSymbols) {
            if (typeSymbol.equals(overridenMethodSymbol.owner()))
                return true;
        }
        return false;
    }

    /**
     * @return ordered type's inheritance hierarchy
     */
    private static List<Type> getInterfaceHierarchy(TypeSymbol typeSymbol) {
        return typeSymbol.interfaces();
        // TODO: go up the hierarchy
    }

    // VERBOSE

    private static final boolean V = false;

    private static void v(String m) {
        if (V) System.out.println(m);
    }
}
