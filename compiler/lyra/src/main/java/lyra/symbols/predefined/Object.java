package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.UnresolvedType;
import lyra.symbols.VariableSymbol;

public class Object extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Object", scope, null);
        c.setBinaryNamePrefix("lyra/runtime");
        c.setAbstract(true);
        try {
            c.define(new VariableSymbol("__id", new UnresolvedType("Number")));
            forwardMethod(c, "constructor", "void", false);
            forwardMethod(c, "toString", "String", false);
            forwardMethod(c, "__equals", "Bool", true);
            forwardMethod(c, "__notequals", "Bool", true);
            forwardMethod(c, "__is", "Bool", true);
            defineClass(scope, c);

            defineGlobal(scope, new VariableSymbol("null", c) {
                @Override
                public String getBinaryName() {
                    return "lyra/runtime/Start/lyra_null";
                }
            });
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }
    }

}
