package lyra.symbols;

/**
 * Symbol class for class member variables and for method-local variables
 */
public class VariableSymbol extends Symbol {
    private TypeSymbol type;

    public VariableSymbol(String name, TypeSymbol type) {
        super(name, SymbolType.VARIABLE);
        this.type = type;
    }

    public TypeSymbol getType() {
        return type;
    }

    /**
     * Replaces this variable TypeSymbol with a new instance. The new instance must refer to the same
     * qualified name, but may have a different TypeSymbol subclass.
     *
     * This is used mostly to convert UnresolvedType's into proper types.
     *
     * @param type new TypeSymbol.
     */
    public void upgradeType(TypeSymbol type) {
        if (!this.type.getQualifiedName().equals(type.getQualifiedName()))
            throw new RuntimeException("upgraded type must have same qualified name.");
        this.type = type;
    }

}
