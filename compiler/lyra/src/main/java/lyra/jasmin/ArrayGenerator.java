package lyra.jasmin;

import lyra.CodeGenerator;
import lyra.symbols.*;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class ArrayGenerator implements CodeGenerator {
    private ClassSymbol classSymbol;
    private MethodHelper methodHelper;
    PrintWriter out;
    SymbolTable table;
    int dimensions;
    TypeSymbol elementType;
    ClassSymbol innerArray;
    ClassSymbol baseArray;

    public ArrayGenerator(ClassSymbol classSymbol) {
        this.classSymbol = classSymbol;
    }

    @Override
    public Symbol getSymbol() {
        return classSymbol;
    }

    @Override
    public void generate(PrintWriter out, SymbolTable table) {
        this.out = out;
        this.table = table;
        this.baseArray = table.getPredefinedClass("Array");

        ArrayClassFactory factory = table.getArrayClassFactory();
        dimensions = factory.getDimensionsFromArrayTypeName(classSymbol.getName());
        elementType = (TypeSymbol)classSymbol.getEnclosingScope().resolve(
                factory.getElementTypeFromArrayTypeName(classSymbol.getName())
        );
        innerArray = (dimensions > 1) ? factory.getArrayOf(elementType, dimensions-1) : null;

        List<MethodSymbol> methods = classSymbol.getMethods()
                .filter(m -> m.getEnclosingScope() == classSymbol).collect(Collectors.toList());
        for (MethodSymbol method : methods) {
            methodBegin(method);
            switch (method.getName()) {
                case "constructor":
                    generateConstructor(method);
                    break;
                case "length":
                case "at":
                case "__at":
                case "set":
                case "__set":
                    generateCastOfSuper(method);
                    break;
                default:
                    throw new RuntimeException("Don't know how to generate method \""
                            + method.getName() + "\".");
            }
            methodEnd();
        }
    }

    private void methodBegin(MethodSymbol methodSymbol) {
        methodHelper = new MethodHelper(out, methodSymbol, table);
        methodHelper.writeHeader();
        out = methodHelper.createBodyWriter();
        methodHelper.writeVars();
    }

    private void methodEnd() {
        out = methodHelper.writePreludeAndBody();
        methodHelper.writePrologue();
        methodHelper = null;
    }

    private void generateConstructor(MethodSymbol method) {
        ClassSymbol intClass = table.getPredefinedClass("Int");
        boolean allInts = method.getArgumentTypes().stream().allMatch(t -> t.isA(intClass));
        if (!allInts || method.getArguments().size() < 1)
            throw new RuntimeException("Unsupported constructor");

        MethodSymbol parent = classSymbol.getSuperClass().resolveOverload("constructor", intClass);
        methodHelper.loadVar((VariableSymbol) method.resolve("this"));
        VariableSymbol dim0 = method.getArguments().get(0);
        methodHelper.loadVar(dim0);
        out.printf("invokespecial %1$s\n", Utils.methodSpec(parent));
        methodHelper.decStackUsage(2);

        if (method.getArguments().size() > 1) {
            List<VariableSymbol> innerDims = method.getArguments().stream().skip(1)
                    .collect(Collectors.toList());
            List<TypeSymbol> innerArgs = innerDims.stream().map(v -> v.getType())
                    .collect(Collectors.toList());
            String intValueOf = "lyra/runtime/Int/valueOf()I";
            MethodSymbol intInc = intClass.resolveOverload("__inc");
            MethodSymbol innerCtor = innerArray.resolveOverload("constructor", innerArgs);
            MethodSymbol set = classSymbol.resolveOverload("set", intClass, innerArray);

            /* generate a for calling set() with new instances of the inner Array */

            /* for initialization */
            VariableSymbol i = methodHelper.createTempVar(intClass);
            out.printf("bipush 0\n");
            methodHelper.storeVar(i);

            /* for condition */
            out.printf("ForStart:\n");
            methodHelper.loadVar(i);
            out.printf("invokevirtual %1$s\n", intValueOf);
            methodHelper.loadVar(dim0);
            out.printf("invokevirtual %1$s\n", intValueOf);
            out.printf("if_icmpeq ForEnd\n");
            methodHelper.decStackUsage(2);

            /* for body */
            methodHelper.loadVar((VariableSymbol) method.resolve("this"));
            methodHelper.loadVar(i);
            methodHelper.incStackUsage(2 /* new + dup */);
            out.printf("new %1$s\ndup\n", innerArray.getBinaryName());
            for (VariableSymbol arg : innerDims) methodHelper.loadVar(arg);
            out.printf("invokespecial %1$s\n", Utils.methodSpec(innerCtor));
            methodHelper.decStackUsage(innerDims.size() + 1 /*dup*/);
            out.printf("invokevirtual %1$s\n", Utils.methodSpec(set));
            methodHelper.decStackUsage(3); /* this, i, new */

            /* for post-loop */
            methodHelper.loadVar(i);
            out.printf("invokevirtual %1$s\n", Utils.methodSpec(intInc));
            methodHelper.storeVar(i);
            out.printf("goto ForStart:\n");

            /* for end */
            out.printf("ForDone:\n");
        }
    }

    private void generateCastOfSuper(MethodSymbol method) {
        MethodSymbol superMethod = baseArray.resolveOverload(method.getName(),
                method.getArgumentTypes());
        methodHelper.loadVar((VariableSymbol)method.resolve("this"));
        method.getArguments().forEach(a -> methodHelper.loadVar(a));
        out.printf("invokespecial %1$s\n", Utils.methodSpec(superMethod));
        methodHelper.decStackUsage(method.getArguments().size());

        if (superMethod.getReturnType() != method.getReturnType())
            out.printf("checkcast %1$s\n", method.getReturnType().getBinaryName());
        out.printf("areturn\n", method.getReturnType().getBinaryName());
    }
}
