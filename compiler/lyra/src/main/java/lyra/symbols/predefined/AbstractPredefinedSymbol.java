package lyra.symbols.predefined;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPredefinedSymbol implements PredefinedSymbol {
    private ArrayList<ClassSymbol> classes = new ArrayList<>();
    private ArrayList<VariableSymbol> vars = new ArrayList<>();
    private ArrayList<UnresolvedType> unresolved = new ArrayList<>();

    protected class ArgumentStrings {
        public String type;
        public String name;

        public ArgumentStrings(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }


    protected void forwardMethod(ClassSymbol classSymbol, String name,
                                 String returnType, boolean infix,
                                 ArgumentStrings... args) throws SemanticErrorException {
        MethodSymbol symbol = new MethodSymbol(name, getType(returnType), classSymbol);
        symbol.setInfix(infix);
        for (ArgumentStrings pair : args)
            symbol.addArgument(new VariableSymbol(pair.name, getType(pair.type)));
        classSymbol.define(symbol);
    }

    protected void defineClass(Scope scope, ClassSymbol symbol) throws SemanticErrorException {
        classes.add(symbol);
        scope.define(symbol);
    }
    protected void defineGlobal(Scope scope, VariableSymbol symbol) throws SemanticErrorException {
        vars.add(symbol);
        scope.define(symbol);
    }
    protected UnresolvedType getType(String name) {
        UnresolvedType type = new UnresolvedType(name);
        unresolved.add(type);
        return type;
    }

    @Override
    public void resolveTypes(Scope scope) {
        ArrayList<TypeSymbol> resolved = new ArrayList<>();
        for (UnresolvedType unresolvedType : unresolved)
            resolved.add((TypeSymbol) scope.resolve(unresolvedType.getName()));

        for (ClassSymbol symbol : classes) resolved.forEach(symbol::upgradeType);
        for (VariableSymbol symbol : vars) resolved.forEach(symbol::upgradeType);
    }
}
