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

    private Compiler analyse() throws IOException {
        Compiler compiler = new Compiler();
        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("samples/MethodOverloads.ly");
        compiler.init(new InputStreamReader(stream));
        compiler.analyse();
        return compiler;
    }
}
