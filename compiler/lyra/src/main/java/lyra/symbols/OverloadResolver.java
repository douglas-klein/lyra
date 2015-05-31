package lyra.symbols;


import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class OverloadResolver {
    static private MethodSymbol resolveImpl(Stream<MethodSymbol> overloads,
                                     int argIdx,
                                     List<TypeSymbol> argTypes,
                                     boolean allowConvertible) {
        //terminating condition
        if (argTypes.isEmpty()) {
            List<MethodSymbol> list = overloads.limit(2).collect(Collectors.toList());
            return list.size() == 1 ? list.get(0) : null;
        }

        TypeSymbol head = argTypes.get(0);
        List<TypeSymbol> tail = argTypes.stream().skip(1).collect(Collectors.toList());

        //split overloads into a isA and convertible
        List<MethodSymbol> isA = new LinkedList<>();
        List<MethodSymbol> convertible = new LinkedList<>();
        for (Iterator<MethodSymbol> it = overloads.iterator(); it.hasNext(); ) {
            MethodSymbol m = it.next();
            TypeSymbol argType = m.getArgumentTypes().get(argIdx);
            if (argType.isA(head)) {
                isA.add(m);
            } else if (allowConvertible && argType.convertible(head)) {
                convertible.add(m);
            }
        }

        //try isA
        MethodSymbol method = resolveImpl(isA.stream(), argIdx + 1, tail, allowConvertible);

        //try convertiblee
        if (method == null && allowConvertible)
            method = resolveImpl(convertible.stream(), argIdx + 1, tail, allowConvertible);

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
