package lyra.symbols;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class OverloadResolver {
    static private MethodSymbol resolveImpl(Stream<MethodSymbol> overloads,
                                     int argIdx,
                                     List<TypeSymbol> argTypes,
                                     boolean allowConvertible) {
        //terminating condition
        if (argTypes.isEmpty()) {
            if (overloads.skip(1).findFirst().isPresent())
                return null; //ambiguous resolution
            Optional<MethodSymbol> first = overloads.findFirst();
            return first.isPresent() ? first.get() : null;
        }

        TypeSymbol head = argTypes.get(0);
        List<TypeSymbol> tail = argTypes.stream().skip(1)
                .collect(Collectors.toCollection(ArrayList<TypeSymbol>::new));

        //try isA
        Stream<MethodSymbol> filtered = overloads
                .filter(m -> m.getArgumentTypes().get(argIdx).isA(head));
        MethodSymbol method = resolveImpl(filtered, argIdx + 1, tail, allowConvertible);

        //try convertible
        if (method == null && allowConvertible) {
            filtered = overloads.filter(m -> m.getArgumentTypes().get(argIdx).convertible(head));
            method = resolveImpl(filtered, argIdx + 1, tail, allowConvertible);
        }
        return method;
    }

    static public MethodSymbol resolve(Stream<MethodSymbol> overloads,
                                Collection<TypeSymbol> argTypes,
                                boolean allowConvertible) {
        ArrayList<TypeSymbol> typesList = argTypes.stream()
                .collect(Collectors.toCollection(ArrayList<TypeSymbol>::new));
        overloads = overloads.filter(m -> m.getArgumentTypes().size() == typesList.size());
        return resolveImpl(overloads, 0, typesList, allowConvertible);
    }
}
