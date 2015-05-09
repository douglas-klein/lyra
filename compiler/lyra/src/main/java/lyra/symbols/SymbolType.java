package lyra.symbols;

/**
 * Possible types of symbols, with pretty names.
 *
 * Used by Symbol for the default toString() and by Symbol users' for runtime subtype testing.
 */
public enum SymbolType {
    CLASS("Class"),
    INTERFACE("Interface"),
    METHOD("Method"),
    VARIABLE("Variable"),
    UNRESOLVED_TYPE("UnresolvedType");


    private String prettyName;

    SymbolType(String prettyName) {
        this.prettyName = prettyName;
    }

    @Override
    public String toString() {
        return prettyName;
    }
}

