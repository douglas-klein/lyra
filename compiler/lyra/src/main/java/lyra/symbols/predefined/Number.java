package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;

public class Number extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Number", scope, (ClassSymbol)scope.resolve("Object"));
        c.setBinaryNamePrefix("lyra/runtime");
        try {
            forwardMethod(c, "toString",         "String", false);
            forwardMethod(c, "__inc",            "Number", true);
            forwardMethod(c, "__dec",            "Number", true);
            forwardMethod(c, "__not",            "Bool",   true);
            forwardMethod(c, "__positive",       "Number", true);
            forwardMethod(c, "__negative",       "Number", true);
            forwardMethod(c, "__mutiplied",      "Number", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__divided",        "Number", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__remainder",      "Number", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__added",          "Number", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__subtracted",     "Number", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__less",           "Bool", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__lessorequal",    "Bool", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__equals",         "Bool", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__notequals",      "Bool", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__greaterorequal", "Bool", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "__greater",        "Bool", true, new ArgumentStrings("Number", "rhs"));
            forwardMethod(c, "toString",         "String", false);
            defineClass(scope, c);
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }
    }
}
