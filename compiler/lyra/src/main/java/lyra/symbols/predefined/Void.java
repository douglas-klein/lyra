package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.ClassSymbol;

public class Void extends AbstractPredefinedSymbol {

    @Override
    public void forward(Scope scope) {
        ClassSymbol c = new ClassSymbol("void", scope,
                (ClassSymbol)scope.resolve("Object")) {
            /**
             * Since java does not allow lower case class names, we quietly hide the problem
             * with this anonymous class.
             */
            @Override
            public String getBinaryName() {
                return "lyra/runtime/Void";
            }
        };
        c.setBinaryNamePrefix("lyra/runtime");
        c.setFinal(true);
        c.setAbstract(true);
        try {
            defineClass(scope, c);
        } catch (SemanticErrorException e) {
            throw new RuntimeException("Compiler not obeying it's own rules.", e);
        }
    }
}
