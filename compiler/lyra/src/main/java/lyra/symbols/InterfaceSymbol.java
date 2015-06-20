package lyra.symbols;

import lyra.SemanticErrorException;
import lyra.scopes.Scope;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Symbol for interfaces (interfacedecl on the grammar).
 */
public class InterfaceSymbol extends TypeSymbol {
    private LinkedHashMap<String, List<MethodSymbol>> members = new LinkedHashMap<>();
    List<InterfaceSymbol> superInterfaces = new LinkedList<>();
    private String binaryNamePrefix = "lyra/user";

    public InterfaceSymbol(String name, Scope enclosingScope) {
        super(name, SymbolType.INTERFACE, enclosingScope);
    }

    public void addSuperInterfaces(InterfaceSymbol superInterface) {
        superInterfaces.add(superInterface);
    }
    public Collection<InterfaceSymbol> getSuperInterfaces() {
        return superInterfaces;
    }

    @Override
    public VariableSymbol resolveField(String name) {
        /* no fields on interfaces */
        return null;
    }
    @Override
    public Symbol shallowResolve(String name) {
        /* no fields on interfaces */
        return null;
    }

    private HashSet<CandidateMethodSymbol> getMethodsImpl(String methodName) {
        //See ClassSymbol.getMethodsImpl().

        HashSet<CandidateMethodSymbol> set = members.get(methodName).stream()
                .map(m -> new CandidateMethodSymbol(m))
                .collect(Collectors.toCollection(HashSet<CandidateMethodSymbol>::new));
        superInterfaces.forEach(i -> i.getMethodsImpl(methodName).forEach(m -> set.add(m)));
        return set;
    }
    public Stream<MethodSymbol> getMethods(String methodName) {
        return getMethodsImpl(methodName).stream().map(c -> c.getWrapped());
    }
    private HashSet<CandidateMethodSymbol> getMethodsImpl() {
        List<MethodSymbol> list = new LinkedList<>();
        members.values().forEach(l -> list.addAll(l));
        HashSet<CandidateMethodSymbol> set = list.stream()
                .map(m -> new CandidateMethodSymbol(m))
                .collect(Collectors.toCollection(HashSet<CandidateMethodSymbol>::new));
        superInterfaces.forEach(i -> i.getMethodsImpl().forEach(m -> set.add(m)));
        return set;
    }
    public Stream<MethodSymbol> getMethods() {
        return getMethodsImpl().stream().map(c -> c.getWrapped());
    }

    @Override
    public MethodSymbol resolveOverload(String name, Collection<TypeSymbol> argTypes) {
        return OverloadResolver.resolve(getMethods(name), argTypes, true);
    }

    @Override
    public String getBinaryNamePrefix() {
        return binaryNamePrefix;
    }
    public void getBinaryNamePrefix(String binaryNamePrefix) {
        this.binaryNamePrefix = binaryNamePrefix;
    }

    @Override
    public boolean isA(TypeSymbol type) {
        if (type == this)
            return true;
        if (superInterfaces.stream().anyMatch(i -> i.isA(type)))
            return true;
        return false;
    }

    @Override
    public boolean converts(TypeSymbol type) {
        return false;
    }

    @Override
    public void define(Symbol sym) throws SemanticErrorException {
        if (!(sym instanceof MethodSymbol)) {
            throw new SemanticErrorException("Interfaces may only contain methods");
        }
        List<MethodSymbol> list = members.get(sym.getName());
        if (list == null) {
            list = new LinkedList<>();
            members.put(sym.getName(), list);
        }
        list.add((MethodSymbol)sym);
    }

    public void upgradeType(TypeSymbol typeSymbol) {
        boolean upgrade = false;
        for (List<MethodSymbol> overloads : members.values()) {
            for (MethodSymbol method : overloads) {
                method.upgradeType(typeSymbol);
            }
        }

        for (InterfaceSymbol superIface : superInterfaces)
            superIface.upgradeType(typeSymbol);
    }
}
