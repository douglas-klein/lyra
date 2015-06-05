package lyra.symbols;

/**
 * Symbol implementation for classes.
 */
import lyra.SemanticErrorException;
import lyra.scopes.Scope;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ClassSymbol extends TypeSymbol {
    /** This is the superclass not enclosingScope field. We still record
     *  the enclosing scope so we can push in and pop out of class defs. */
    ClassSymbol superClass;

    /** Interface implemented by the class, may be null. */
    private InterfaceSymbol interfaceSymbol;

    /** List of all fields and methods */
    public Map<String,List<Symbol>> members = new LinkedHashMap<>();
    private boolean aFinal;
    private boolean aAbstract;

    public ClassSymbol(String name, Scope enclosingScope, ClassSymbol superClass) {
        super(name, SymbolType.CLASS, enclosingScope);
        this.superClass = superClass;
    }

    public void setSuperClass(ClassSymbol superClass) {
        this.superClass = superClass;
    }

    @Override
    public VariableSymbol resolveField(String name) {
        Symbol sym = shallowResolve(name);
        if (sym != null)
            return (VariableSymbol)sym;
        if (superClass != null)
            return superClass.resolveField(name);
        return null;
    }

    @Override
    public Symbol shallowResolve(String name) {
        //by semantic rules, a field name must be unique inside a class
        List<Symbol> list = members.get(name);
        if (list == null) return null;

        Optional<Symbol> symbol = list.stream()
                .filter(s -> s instanceof VariableSymbol).findFirst();
        return symbol.isPresent() ? symbol.get() : null;
    }

    @Override
    public boolean isA(TypeSymbol type) {
        if (type == this)
            return true;
        if (superClass != null && superClass.isA(type))
            return true;
        if (interfaceSymbol != null && interfaceSymbol.isA(type))
            return true;
        return false;
    }

    @Override
    public boolean converts(TypeSymbol type) {
        ArrayList<TypeSymbol> args = new ArrayList<>();
        args.add(type);
        Stream<MethodSymbol> overloads = getOverloads("constructor");

        //Resolve the overload without resorting to convertible()
        return OverloadResolver.resolve(overloads, args, false) != null;
    }

    /**
     * Gets the set of all methods in the inheritance hierarchy that have the given \p name
     * and the given \p arity number of arguments.
     */
    private Stream<MethodSymbol> getOverloads(String methodName) {
        return getOverloadsImpl(methodName).stream().map(c -> c.getWrapped());
    }

    private HashSet<CandidateMethodSymbol> getOverloadsImpl(String methodName) {

        HashSet<CandidateMethodSymbol> set;
        List<Symbol> list = members.get(methodName);
        if (list == null) {
            set = new HashSet<>();
        } else {
            set = list.stream()
                    .filter(s -> s instanceof MethodSymbol)
                    .map(s -> new CandidateMethodSymbol((MethodSymbol) s))
                    .collect(Collectors.toCollection(HashSet<CandidateMethodSymbol>::new));
        }

        /* Once we got the methods from this class, check the parent and add only the parent
         * (and indirect parent) methods that do not have exact match of argument types. This is
         * the reason for collecting the stream into a set, and ensures that the most specialized
         * overloads of every method will be on the returned set.
         *
         * Having the most specialized overloads is important due to return type polymorphism: any
         * overload on the inheritance chain must have a return type that is the same or a
         * specialization of the most generic return type already used for this method name up
         * in the inheritance chain (including the current class). */
        if (superClass != null)
            superClass.getOverloadsImpl(methodName).forEach(m -> set.add(m));

        return set;
    }

    @Override
    public MethodSymbol resolveOverload(String methodName, Collection<TypeSymbol> argTypes) {
        ArrayList<TypeSymbol> typesList = argTypes.stream()
                .collect(Collectors.toCollection(ArrayList<TypeSymbol>::new));
        return OverloadResolver.resolve(getOverloads(methodName), typesList, true);
    }

    @Override
    public void define(Symbol sym) throws SemanticErrorException {
        if (sym instanceof VariableSymbol) {
            if (members.containsKey(sym.getName())) {
                throw new SemanticErrorException("name \"" + sym.getName() + "\" has already been used on this class");
            }
            LinkedList<Symbol> list = new LinkedList<>();
            list.add(sym);
            members.put(sym.getName(), list);
        } else if (sym instanceof MethodSymbol) {
            List<Symbol> list;
            if (members.containsKey(sym.getName())) {
                list = members.get(sym.getName());
                if (!list.isEmpty() && !(list.get(0) instanceof MethodSymbol)) {
                    throw new SemanticErrorException("Using name \"" + sym.getName() + "\" previously used for something that was not a method");
                }
            } else {
                list = new LinkedList<>();
                members.put(sym.getName(), list);
            }
            list.add(sym);
        } else {
            throw new SemanticErrorException("A class may only contain methods and fields. \"" + sym.getName() + "\" is neither.");
        }
    }

    public InterfaceSymbol getInterfaceSymbol() {
        return interfaceSymbol;
    }

    public void setInterfaceSymbol(InterfaceSymbol interfaceSymbol) {
        this.interfaceSymbol = interfaceSymbol;
    }

    public boolean isFinal() {
        return aFinal;
    }

    public void setFinal(boolean aFinal) {
        this.aFinal = aFinal;
    }

    public void setAbstract(boolean anAbstract) {
        this.aAbstract = anAbstract;
    }

    public boolean isAbstract() {
        return aAbstract;
    }

    /**
     * Replaces any type used inside this class (recursing into methods) that has the same
     * qualified name as the given type with the given type.
     *
     * @param type new TypeSymbol
     */
    public void upgradeType(TypeSymbol type) {
        if (interfaceSymbol != null) {
            if (interfaceSymbol.getQualifiedName().equals(type.getQualifiedName()))
                interfaceSymbol = (InterfaceSymbol) type;
        }

        for (List<Symbol> list : members.values()) {
            for (Symbol symbol : list) {
                if (symbol instanceof MethodSymbol)
                    ((MethodSymbol) symbol).upgradeType(type);
                if (symbol instanceof VariableSymbol)
                    ((VariableSymbol) symbol).upgradeType(type);
            }
        }
    }
}
