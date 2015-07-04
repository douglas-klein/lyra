package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;

public class LyraString extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("String", scope, (ClassSymbol)scope.resolve("Object"));
        c.setBinaryNamePrefix("lyra/runtime");
        try {
            forwardMethod(c, "length", "Int",    false);

            forwardMethod(c, "__at",   "String", true, new ArgumentStrings("Int", "idx"));
            forwardMethod(c, "__set",  "void",   true, new ArgumentStrings("Int", "idx"),
                                                       new ArgumentStrings("String", "char"));
            forwardMethod(c, "at",     "String", true, new ArgumentStrings("Int", "idx"));
            forwardMethod(c, "set",    "void",   true, new ArgumentStrings("Int", "idx"),
                                                       new ArgumentStrings("String", "char"));

            forwardMethod(c, "__less",           "Bool", true, new ArgumentStrings("String", "rhs"));
            forwardMethod(c, "__lessorequal",    "Bool", true, new ArgumentStrings("String", "rhs"));
            forwardMethod(c, "__equals",         "Bool", true, new ArgumentStrings("String", "rhs"));
            forwardMethod(c, "__notequals",      "Bool", true, new ArgumentStrings("String", "rhs"));
            forwardMethod(c, "__greaterorequal", "Bool", true, new ArgumentStrings("String", "rhs"));
            forwardMethod(c, "__greater",        "Bool", true, new ArgumentStrings("String", "rhs"));

            forwardMethod(c, "parseInt",    "Int", false);
            forwardMethod(c, "parseNumber", "Int", false);
            forwardMethod(c, "parseBool",   "Int", false);

            forwardMethod(c, "toString",    "String", false);

            defineClass(scope, c);
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }
    }

}
