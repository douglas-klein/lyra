package lyra.symbols.predefined;

import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;
import lyra.symbols.UnresolvedType;
import lyra.symbols.VariableSymbol;

public class Object extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("Object", scope, null);
        c.setAbstract(true);
        c.define(new VariableSymbol("__id", new UnresolvedType("Number")));
        forwardMethod(c, "toString", "String", false);
        forwardMethod(c, "__equals", "Bool", true);
        forwardMethod(c, "__notequals", "Bool", true);
        forwardMethod(c, "__is", "Bool", true);
        defineClass(scope, c);

        defineGlobal(scope, new VariableSymbol("null", c));
    }

}
