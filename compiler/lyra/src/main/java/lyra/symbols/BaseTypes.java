package lyra.symbols;

import java.util.HashMap;

/**
 * Created by eduardo on 30/04/15.
 *
 * TODO replace this with injected symbols on the root scope.
 */
public class BaseTypes  {
    HashMap<String, UnresolvedType> types;

    public BaseTypes() {
        types = new HashMap<>();
        types.put("Void", new UnresolvedType("Void"));
    }
    
    public UnresolvedType get(String type){
        return types.get(type);
    }
}
