package lyra.symbols;

/**
 * Symbol class for class member variables and for method-local variables
 */
public class VariableSymbol extends Symbol {
    private TypeSymbol type;
    private boolean classField = false;

    public VariableSymbol(String name, TypeSymbol type) {
        super(name, SymbolType.VARIABLE);
        this.type = type;
    }

    public TypeSymbol getType() {
        return type;
    }

    /**
     * Replaces this variable TypeSymbol with a new instance. The new instance must refer to the
     * same qualified name, but may have a different TypeSymbol subclass. If \p type does
     * not obeys this restrictions, the upgrade is not done.
     *
     * This is used mostly to convert UnresolvedType's into proper types.
     *
     * @param type new TypeSymbol.
     * @return true iff the upgrade took place.
     */
    public boolean upgradeType(TypeSymbol type) {
        if (this.type.getQualifiedName().equals(type.getQualifiedName())) {
            this.type = type;
            return true;
        }
        return false;
    }

    public boolean isClassField() {
        return classField;
    }

    public void setClassField(boolean classField) {
        this.classField = classField;
    }

    public String getBinaryName() {
        return "lyra_" + getName();
    }
}
