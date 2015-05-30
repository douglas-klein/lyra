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

    public InterfaceSymbol(String name, Scope enclosingScope) {
        //TODO we know the scope where we were declared, but where are our super-interfaces?
        super(name, SymbolType.INTERFACE, enclosingScope);
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

    private HashSet<CandidateMethodSymbol> getOverloadsImpl(String methodName) {
        //See ClassSymbol.getOverloadsImpl().

        HashSet<CandidateMethodSymbol> set = members.get(methodName).stream()
                .map(m -> new CandidateMethodSymbol(m))
                .collect(Collectors.toCollection(HashSet<CandidateMethodSymbol>::new));
        superInterfaces.stream()
                .forEach(i -> i.getOverloadsImpl(methodName).stream()
                        .forEach(m -> set.add(m)));
        return set;
    }
    private Stream<MethodSymbol> getOverloads(String methodName) {
        return getOverloadsImpl(methodName).stream().map(c -> c.getWrapped());
    }

    @Override
    public MethodSymbol resolveOverload(String name, Collection<TypeSymbol> argTypes) {
        return OverloadResolver.resolve(getOverloads(name), argTypes, true);
    }

    @Override
    public boolean isA(TypeSymbol type) {
        if (type == this)
            return true;
        //TODO handle super-interfaces
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
}
