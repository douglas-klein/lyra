package lyra.symbols;

/**
 * Created by eduardo on 29/04/15.
 */
public class Type {
    String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName(){return this.name;}

    @Override
    public String toString() {
        if (name != null)
            return  name ;
        else
            return "";
    }
}
