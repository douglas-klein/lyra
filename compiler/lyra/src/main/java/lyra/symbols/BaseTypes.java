package lyra.symbols;

import java.util.HashMap;

/**
 * Created by eduardo on 30/04/15.
 */
public class BaseTypes  {
    HashMap<String, Type> types;

    public BaseTypes() {
        types = new HashMap<>();
        types.put("Void", new Type("Void"));
    }
    
    public Type get(String type){
        return types.get(type);
    }
}
