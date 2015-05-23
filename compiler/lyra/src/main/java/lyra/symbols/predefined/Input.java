package lyra.symbols.predefined;

import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.VariableSymbol;

public class Input extends AbstractPredefinedSymbol {
    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Input", scope, (ClassSymbol)scope.resolve("Object"));
        forwardMethod(c, "open", "Bool", false, new ArgumentStrings("String", "filename"));
        forwardMethod(c, "hasError", "Bool", false);
        forwardMethod(c, "peek", "String", false);
        forwardMethod(c, "read", "String", false, new ArgumentStrings("Number", "count"));
        forwardMethod(c, "readLine", "String", false);
        defineClass(scope, c);

        defineGlobal(scope, new VariableSymbol("in", c));
    }

}
