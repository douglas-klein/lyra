package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.VariableSymbol;

public class Output extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Output", scope, (ClassSymbol)scope.resolve("Object"));
        try {
            forwardMethod(c, "open", "Bool", false, new ArgumentStrings("String", "filename"));
            forwardMethod(c, "close", "Void", false);
            forwardMethod(c, "flush", "Void", false);uu
            forwardMethod(c, "hasError", "Bool", false);
            forwardMethod(c, "write",   "void", false, new ArgumentStrings("String", "text"));
            forwardMethod(c, "write",   "void", false, new ArgumentStrings("Object", "obj"));
            forwardMethod(c, "writeln", "void", false, new ArgumentStrings("String", "text"));
            forwardMethod(c, "writeln", "void", false, new ArgumentStrings("Object", "obj"));
            defineClass(scope, c);

            defineGlobal(scope, new VariableSymbol("out", c));
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }
    }

}
