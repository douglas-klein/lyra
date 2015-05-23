package lyra.symbols.predefined;

import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.VariableSymbol;

public class Output extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Output", scope, (ClassSymbol)scope.resolve("Object"));
        forwardMethod(c, "open", "Bool", false, new ArgumentStrings("String", "filename"));
        forwardMethod(c, "hasError", "Bool", false);
        forwardMethod(c, "write", "void", false, new ArgumentStrings("String", "text"));
        forwardMethod(c, "write", "void", false, new ArgumentStrings("Object", "obj"));
        defineClass(scope, c);

        defineGlobal(scope, new VariableSymbol("out", c));
    }

}
