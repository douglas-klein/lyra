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
            return list.size() == 1 ? list.get(0) : fixAmbiguity(list, 0);
        }

        TypeSymbol head = argTypes.get(0);
        List<TypeSymbol> tail = argTypes.stream().skip(1).collect(Collectors.toList());

        //split overloads into a isA and convertible
        List<MethodSymbol> isA = new LinkedList<>();
        List<MethodSymbol> convertible = new LinkedList<>();
        for (Iterator<MethodSymbol> it = overloads.iterator(); it.hasNext(); ) {
            MethodSymbol m = it.next();
            TypeSymbol argType = m.getArgumentTypes().get(argIdx);
            if (head.isA(argType)) {
                isA.add(m);
            } else if (allowConvertible && head.convertible(argType)) {
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

    private static MethodSymbol fixAmbiguity(List<MethodSymbol> list, int argIdx) {
        if (list.size() == 0)
            return null;

        int arity = list.get(0).getArgumentTypes().size();
        if (argIdx == arity)
            return list.size() == 1 ? list.get(0) : null;

        //Find the most specialized of all argIdx-th arguments of the candidates
        TypeSymbol mostSpecialized = list.get(0).getArgumentTypes().get(argIdx);
        for (int i = 1; i < list.size(); ++i) {
            TypeSymbol candidate = list.get(i).getArgumentTypes().get(argIdx);
            boolean candidateIsA = candidate.isA(mostSpecialized);
            boolean mostIsA = mostSpecialized.isA(candidate);
            if (candidateIsA && !mostIsA)
                mostSpecialized = candidate;
            else if (!candidateIsA && !mostIsA)
                return null; //ambiguity
        }
        final TypeSymbol finalMostSpecialized = mostSpecialized;

        List<MethodSymbol> selected = list.stream()
                .filter(m -> m.getArgumentTypes().get(argIdx).equals(finalMostSpecialized))
                .collect(Collectors.toList());
        return fixAmbiguity(selected, argIdx+1);
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
