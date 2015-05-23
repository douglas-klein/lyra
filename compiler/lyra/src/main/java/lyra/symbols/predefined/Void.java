package lyra.symbols.predefined;

import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;

public class Void extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("void", scope,
                (ClassSymbol)scope.resolve("Object"));
        c.setFinal(true);
        c.setAbstract(true);
        defineClass(scope, c);
    }
}
