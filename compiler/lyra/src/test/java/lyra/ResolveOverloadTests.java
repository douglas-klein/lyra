package lyra;

import lyra.listeners.ArrayRewriterListener;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.TypeSymbol;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResolveOverloadTests {
    @Test
    public void testSingleCandidateResolve() throws Exception {
        Compiler compiler = analyse();
        Scope global = compiler.getSymbolTable().getGlobal();
        Symbol symbol = global.resolve("A");
        assertNotNull(symbol);
        assertTrue(symbol instanceof ClassSymbol);
        ClassSymbol classSymbol = (ClassSymbol) symbol;

        ArrayList<TypeSymbol> args = new ArrayList<>();
        args.add((TypeSymbol)global.resolve("Number"));
        MethodSymbol methodSymbol = classSymbol.resolveOverload("method1", args);

        assertNotNull(methodSymbol);
        assertTrue(CollectionUtils.isEqualCollection(args,
                methodSymbol.getArgumentTypes()));
    }

    @Test
    public void testGetSingleCandidateSpecializedArgumentGeneralizedParameter() throws Exception {
        Compiler compiler = analyse();
        Scope global = compiler.getSymbolTable().getGlobal();
        ClassSymbol classSymbol = (ClassSymbol) global.resolve("A");

        ArrayList<TypeSymbol> args = new ArrayList<>();
        args.add((TypeSymbol)global.resolve("Int"));
        MethodSymbol methodSymbol = classSymbol.resolveOverload("method1", args);

        ArrayList<TypeSymbol> expectedArgs = new ArrayList<>();
        expectedArgs.add((ClassSymbol)global.resolve("Number"));

        assertNotNull(methodSymbol);
        assertTrue(CollectionUtils.isEqualCollection(expectedArgs,
                methodSymbol.getArgumentTypes()));
    }

    @Test
    public void testGetMoreSpecializedOverload() throws Exception {
        Compiler compiler = analyse();
        Scope global = compiler.getSymbolTable().getGlobal();
        ClassSymbol classSymbol = (ClassSymbol) global.resolve("A");

        ArrayList<TypeSymbol> args = new ArrayList<>();
        args.add((ClassSymbol)global.resolve("Int"));
        MethodSymbol methodSymbol = classSymbol.resolveOverload("method2", args);

        assertNotNull(methodSymbol);
        assertTrue(CollectionUtils.isEqualCollection(args, methodSymbol.getArgumentTypes()));
    }

    @Test
    public void testGetMoreSpecializedOverloadFromSuperclass() throws Exception {
        Compiler compiler = analyse();
        Scope global = compiler.getSymbolTable().getGlobal();
        ClassSymbol classSymbol = (ClassSymbol) global.resolve("A");

        ArrayList<TypeSymbol> args  = new ArrayList<>();
        args.add((TypeSymbol)global.resolve("Int"));
        MethodSymbol methodSymbol = classSymbol.resolveOverload("inheritedSpec", args);

        assertNotNull(methodSymbol);
        assertTrue(CollectionUtils.isEqualCollection(args, methodSymbol.getArgumentTypes()));
    }

    @Test
    public void testResolveAmbiguityChoosingFromSecondArg() throws Exception {
        Compiler compiler = analyse();
        Scope global = compiler.getSymbolTable().getGlobal();
        ClassSymbol classSymbol = (ClassSymbol)global.resolve("A");

        ArrayList<TypeSymbol> args = new ArrayList<>();
        args.add((TypeSymbol)global.resolve("Int"));
        args.add((TypeSymbol)global.resolve("Int"));
        MethodSymbol methodSymbol = classSymbol.resolveOverload("method3", args);

        assertNotNull(methodSymbol);
        assertTrue(CollectionUtils.isEqualCollection(args, methodSymbol.getArgumentTypes()));
    }

    @Test
    public void testResolveByIsAThenConvertible() throws Exception {
        Compiler compiler = analyse();
        Scope global = compiler.getSymbolTable().getGlobal();
        ClassSymbol classSymbol = (ClassSymbol)global.resolve("A");

        ArrayList<TypeSymbol> args = new ArrayList<>();
        args.add((TypeSymbol)global.resolve("Int"));
        args.add((TypeSymbol)global.resolve("U"));

        MethodSymbol methodSymbol = classSymbol.resolveOverload("method4", args);

        ArrayList<TypeSymbol> expectedArgs = new ArrayList<>();
        expectedArgs.add((TypeSymbol)global.resolve("Number"));
        expectedArgs.add((TypeSymbol)global.resolve("T"));

        assertNotNull(methodSymbol);
        assertTrue(CollectionUtils.isEqualCollection(expectedArgs,
                methodSymbol.getArgumentTypes()));
    }

    @Test
    public void testResolveIntInc() throws Exception {
        Compiler compiler = analyse();
        Scope global = compiler.getSymbolTable().getGlobal();
        ClassSymbol classSymbol = (ClassSymbol)global.resolve("Int");

        ArrayList<TypeSymbol> args = new ArrayList<>();

        MethodSymbol methodSymbol = classSymbol.resolveOverload("__inc", args);
        assertNotNull(methodSymbol);
    }

    @Test
    public void testEnumEquality() throws Exception {
        Compiler compiler = analyse("samples/Enums.ly");
        Scope global = compiler.getSymbolTable().getGlobal();
        ClassSymbol classSymbol = (ClassSymbol) global.resolve("RomanNumerals");

        ArrayList<TypeSymbol> args = new ArrayList<>();
        args.add(classSymbol);

        MethodSymbol methodSymbol = classSymbol.resolveOverload("__equals", args);
        assertNotNull(methodSymbol);
    }

    private Compiler analyse() throws Exception {
        return analyse("samples/MethodOverloads.ly");
    }

    private Compiler analyse(String path) throws Exception {
        Compiler compiler = new Compiler();
        ClassLoader loader = getClass().getClassLoader();
        compiler.init(new File(loader.getResource(path).toURI()));
        compiler.analyse();
        return compiler;
    }
}
