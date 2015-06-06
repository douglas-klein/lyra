package lyra;

import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.TypeSymbol;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

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
    public void testSingleCandidateResolve() throws IOException {
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
    public void testGetSingleCandidateSpecializedArgumentGeneralizedParameter() throws IOException {
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
    public void testGetMoreSpecializedOverload() throws IOException {
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
    public void testGetMoreSpecializedOverloadFromSuperclass() throws IOException {
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
    public void testResolveAmbiguityChoosingFromSecondArg() throws IOException {
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
    public void testResolveByIsAThenConvertible() throws IOException {
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

    private Compiler analyse() throws IOException {
        Compiler compiler = new Compiler();
        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("samples/MethodOverloads.ly");
        compiler.init(new InputStreamReader(stream));
        compiler.analyse();
        return compiler;
    }
}
