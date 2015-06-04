package lyra.symbols;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import org.stringtemplate.v4.misc.STRuntimeMessage;

import java.util.HashMap;

/**
 * Generates, keeps and reuses ClassSymbol instances which represent Arrays of things.
 *
 * All generated instances have names following the pattern X$Array, where X is the name of the
 * type of the array, which may be yet another array. Array is the super class of all instances.
 *
 * Instances are reused whenever possible, using the same instance of this class, X$Array will
 * only be produced once, subsequent queries that expect it as a result will receive the same
 * instance.
 */
public class ArrayClassFactory {
    /**
     * All generated ClassSymbol's are kept here, element type -> dimmensions -> instance
     */
    private HashMap<TypeSymbol, HashMap<Integer, ClassSymbol>> arrays = new HashMap<>();
    private final SymbolTable symbolTable;

    public ArrayClassFactory(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    public ClassSymbol getArrayOf(TypeSymbol elementType) {
        return getArrayOf(elementType, 1);
    }
    public ClassSymbol getArrayOf(TypeSymbol elementType, int dimensions) {
        HashMap<Integer, ClassSymbol> map = arrays.get(elementType);
        if (map == null) {
            map = new HashMap<>();
            arrays.put(elementType, map);
        }
        ClassSymbol classSymbol = map.get(new Integer(dimensions));
        if (classSymbol == null) {
            classSymbol = makeArray(elementType, dimensions);
            map.put(new Integer(dimensions), classSymbol);
        }
        return classSymbol;
    }

    private ClassSymbol makeArray(TypeSymbol elementType, int dimensions) {
        return  (dimensions > 1)
                ? makeArray(getArrayOf(elementType, dimensions-1))
                : makeArray(elementType) ;
    }

    private ClassSymbol makeArray(TypeSymbol elementType) {
        String name = elementType.getName() + "$Array";
        ClassSymbol c = new ClassSymbol(name, elementType.getEnclosingScope(), getArrayClass());

        MethodSymbol m = new MethodSymbol("at", elementType, c);
        m.addArgument(new VariableSymbol("idx", getIntClass()));
        c.define(m);

        m = new MethodSymbol("__at", elementType, c);
        m.addArgument(new VariableSymbol("idx", getIntClass()));
        c.define(m);

        m = new MethodSymbol("__set", elementType, c);
        m.addArgument(new VariableSymbol("idx", getIntClass()));
        m.addArgument(new VariableSymbol("value", elementType));
        c.define(m);

        m = new MethodSymbol("set", elementType, c);
        m.addArgument(new VariableSymbol("idx", getIntClass()));
        m.addArgument(new VariableSymbol("value", elementType));
        c.define(m);

        elementType.getEnclosingScope().define(c);

        return c;
    }

    public String getElementTypeFromArrayTypeName(String arrayTypeName) {
        int idx = arrayTypeName.indexOf("$Array");
        if (idx < 0) return null;
        return arrayTypeName.substring(0, idx);
    }
    public int getDimensionsFromArrayTypeName(String arrayTypeName) {
        int count = 0;
        String str = arrayTypeName;
        int idx;
        final String component = "$Array";
        final int componentLength = component.length();

        while ((idx = str.indexOf("$Array")) >= 0) {
            ++count;
            str = str.substring(idx+componentLength, str.length());
        }
        return count;
    }

    private TypeSymbol getIntClass() {
        return symbolTable.getPredefinedClass("Int");
    }

    private ClassSymbol getArrayClass() {
        return symbolTable.getPredefinedClass("Array");
    }

    public static String getArrayTypeName(String elementTypeName, int dimensions) {
        String suffix = "";
        for (int i = 0; i < dimensions; i++)
            suffix += "$Array";
        return elementTypeName + suffix;
    }
}
