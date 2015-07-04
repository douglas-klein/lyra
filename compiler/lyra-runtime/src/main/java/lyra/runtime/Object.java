package lyra.runtime;

public class Object {
    private int id;

    protected Object(int id) {
        this.id = id;
    }
    public Object() {
        this(Start.makeId());
    }

    public Int lyra___id() { return new Int(id); }
    public String lyra_toString() {
        return new String("Object(" + lyra___id().valueOf() + ")");
    }
    public Bool lyra___equals(Object rhs) {
        if (lyra___id().lyra___equals(rhs.lyra___id()).valueOf())
            return Bool._true;
        return Bool._false;
    }
    public Bool lyra___notequals(Object rhs) {
        return lyra___equals(rhs).lyra___not();
    }
    public Bool lyra___is(Object rhs) {
        return lyra___id().lyra___equals(rhs.lyra___id());
    }
}
