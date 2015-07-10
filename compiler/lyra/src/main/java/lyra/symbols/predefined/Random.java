package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;

public class Random extends AbstractPredefinedSymbol  {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Random", scope, (ClassSymbol)scope.resolve("Random"));
        c.setBinaryNamePrefix("lyra/runtime");
        try {
            forwardMethod(c, "constructor", "void", false);
            forwardMethod(c, "toString",     "String", false);
            forwardMethod(c, "nextInt",      "Int", true, new ArgumentStrings("Int", "bound"));
            forwardMethod(c, "nextNumber",   "Number", true);
            defineClass(scope, c);
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }
    }

}
