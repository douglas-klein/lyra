package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.VariableSymbol;

public class Input extends AbstractPredefinedSymbol {
    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Input", scope, (ClassSymbol)scope.resolve("Object"));
        c.setBinaryNamePrefix("lyra/runtime");
        try {
            forwardMethod(c, "open", "Bool", false, new ArgumentStrings("String", "filename"));
            forwardMethod(c, "close", "void", false);
            forwardMethod(c, "isOpen", "Bool", false);
            forwardMethod(c, "hasError", "Bool", false);
            forwardMethod(c, "atEnd", "Bool", false);
            forwardMethod(c, "read", "String", false, new ArgumentStrings("Int", "count"));
            forwardMethod(c, "readLine", "String", false);
            defineClass(scope, c);

            defineGlobal(scope, new VariableSymbol("in", c));
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }

    }

}
