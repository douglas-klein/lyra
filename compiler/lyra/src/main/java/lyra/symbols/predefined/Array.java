package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;

public class Array extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Array", scope, (ClassSymbol)scope.resolve("Object"));
        c.setBinaryNamePrefix("lyra/runtime");
        try {
            forwardMethod(c, "constructor", "void", false, new ArgumentStrings("Int", "size"));
            forwardMethod(c, "__at", "Object", false, new ArgumentStrings("Int", "idx"));
            forwardMethod(c, "at", "Object", false, new ArgumentStrings("Int", "idx"));
            forwardMethod(c, "__set", "Object", false, new ArgumentStrings("Int", "idx"),
                    new ArgumentStrings("Object", "value"));
            forwardMethod(c, "set", "Object", false, new ArgumentStrings("Int", "idx"),
                    new ArgumentStrings("Object", "value"));
            forwardMethod(c, "toString", "String", false);

            defineClass(scope, c);
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }
    }
}
