package lyra.symbols;

import java.util.Iterator;
import java.util.ListIterator;

public class CandidateMethodSymbol  implements Comparable<CandidateMethodSymbol> {
    private MethodSymbol wrapped;

    public CandidateMethodSymbol(MethodSymbol wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MethodSymbol) {
            MethodSymbol rhs = (MethodSymbol) o;
            return rhs.getName().equals(wrapped.getName())
                    && rhs.getArgumentTypes().equals(wrapped.getArgumentTypes());
        } else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        int hash = wrapped.getName().hashCode();
        for (TypeSymbol symbol : wrapped.getArgumentTypes())
            hash = 31*hash + symbol.hashCode();
        return hash;
    }

    @Override
    public int compareTo(CandidateMethodSymbol rhs) {
        int cmp = wrapped.getName().compareTo(rhs.getWrapped().getName());
        if (cmp != 0) return cmp;

        Iterator<TypeSymbol> it1 = wrapped.getArgumentTypes().iterator();
        Iterator<TypeSymbol> it2 = rhs.getWrapped().getArgumentTypes().iterator();
        while (it1.hasNext() && it2.hasNext()) {
            cmp = it1.next().hashCode() - it2.next().hashCode();
            if (cmp != 0) return cmp;
        }
        if (it1.hasNext() && !it2.hasNext()) return 1;
        if (!it1.hasNext() && it2.hasNext()) return -1;
        return 0;
    }

    public MethodSymbol getWrapped() {
        return wrapped;
    }

    public void setWrapped(MethodSymbol wrapped) {
        this.wrapped = wrapped;
    }
}
