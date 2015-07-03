package lyra.jasmin;

import lyra.symbols.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;

/**
 * Helper with common code used throughout the code generation for a method.
 */
public class MethodHelper {
    PrintWriter classWriter;
    PrintWriter writer;
    ByteArrayOutputStream bodyOutputStream;

    private int stackUsage = 0;
    /** Tracks stack usage as the method children are visited, when this grows larger than
     *  methodLocalsUsage, methodLocalsUsage is updated. */
    private int currentStackUsage = 0;

    private HashMap<VariableSymbol, Integer> vars = new HashMap<>();
    private HashMap<ParserRuleContext, Integer> Labels = new HashMap<>();
    private HashMap<ParserRuleContext, Integer> labelsAfter = new HashMap<>();

    MethodSymbol methodSymbol;
    SymbolTable table;

    public MethodHelper(PrintWriter writer, MethodSymbol methodSymbol, SymbolTable table) {
        this.writer = writer;
        this.methodSymbol = methodSymbol;
        this.table = table;
    }

    public PrintWriter createBodyWriter() {
        bodyOutputStream = new ByteArrayOutputStream();
        this.classWriter = writer;
        writer = new PrintWriter(bodyOutputStream);
        return writer;
    }

    public void incStackUsage(int count) {
        currentStackUsage += count;
        if (currentStackUsage > stackUsage)
            stackUsage = currentStackUsage;
    }
    public void decStackUsage(int count) {
        currentStackUsage -= count;
    }

    public void declareVar(VariableSymbol var) {
        if (vars.get(var) != null)
            return;
        int idx = vars.size();
        vars.put(var, new Integer(idx));
        writer.printf(".var %1$d is %2$s %3$s\n", idx, var.getName(), Utils.typeSpec(var.getType()));
    }

    public VariableSymbol createTempVar(TypeSymbol type) {
        VariableSymbol var = new VariableSymbol("lyra_jasmin_temp_" + vars.size(), type);
        declareVar(var);
        return var;
    }

    public void loadVar(VariableSymbol var) {
        Integer idx = vars.get(var);
        incStackUsage(1);
        writer.printf("aload %1$d\n", idx.intValue());
    }
    public void storeVar(VariableSymbol var) {
        Integer idx = vars.get(var);
        writer.printf("astore %1$d\n", idx.intValue());
        decStackUsage(1);
    }
    public String generateLabel(ParserRuleContext node) {
        Labels.put(node, Labels.size());
        return getLabel(node);
    }
    public String getLabel(ParserRuleContext node) {
        Integer integer = Labels.get(node);
        if (integer == null)
            return null;
        return "LyraLabel" + integer.intValue();
    }
    public String generateLabelAfter(ParserRuleContext node) {
        labelsAfter.put(node, labelsAfter.size());
        return getLabelAfter(node);
    }
    public String getLabelAfter(ParserRuleContext node) {
        Integer integer = labelsAfter.get(node);
        if (integer == null)
            return null;
        return "LyraLabelAfter" + integer.intValue();
    }

    public int getStackUsage() {
        return stackUsage;
    }

    public int getVarCount() {
        return vars.size();
    }

    public void writeHeader() {
        writer.printf(".method %1$s %2$s(", Utils.mapVisibility(methodSymbol.getVisibility()),
                methodSymbol.getBinaryName());
        for (TypeSymbol type : methodSymbol.getArgumentTypes())
            writer.print(Utils.typeSpec(type));
        writer.printf(")%1$s\n", methodSymbol.isConstructor() ? "V"
                : Utils.typeSpec(methodSymbol.getReturnType()));
    }

    public PrintWriter writePreludeAndBody() {
        writer.flush();
        String body = "";
        try {
            body = new String(bodyOutputStream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) { }
        writer.close();
        bodyOutputStream = null;
        writer = classWriter;

        writer.printf(".limit stack %1$d\n" +
                ".limit locals %2$d\n" +
                "%3$s\n", getStackUsage(), getVarCount(), body);
        return  writer;
    }

    public void writePrologue() {
        /* inject lyra/runtime/Void return */
        ClassSymbol voidClass = table.getPredefinedClass("void");
        if (methodSymbol.getReturnType() == voidClass && !methodSymbol.isConstructor()) {
            MethodSymbol constructor = voidClass.resolveOverload("constructor");
            writer.printf("new %1$s\n" +
                    "dup\n" +
                    "invokespecial %2$s\n" +
                    "areturn\n", voidClass.getBinaryName(), Utils.methodSpec(constructor));
        } else if (methodSymbol.isConstructor()) {
            writer.printf("return\n");
        }

        writer.printf(".end method\n\n\n");
    }

    public void writeVars() {
        declareVar((VariableSymbol) methodSymbol.resolve("this"));
        for (VariableSymbol arg : methodSymbol.getArguments())
            declareVar(arg);
    }

    public void newLyraInt(int value) {
        ClassSymbol intClass = table.getPredefinedClass("Int");
        incStackUsage(3);
        writer.printf("new %1$s\n" +
                "dup\n" +
                "bipush %2$d\n" +
                "invokespecial %1$s/<init>(I)V\n" +
                "", intClass.getBinaryName(), value);
        decStackUsage(2);
    }
}
