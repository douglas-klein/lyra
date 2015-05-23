package lyra.symbols.predefined;

import lyra.scopes.Scope;

public interface PredefinedSymbol {
    void forward(Scope scope);
    void resolveTypes(Scope scope);
}
