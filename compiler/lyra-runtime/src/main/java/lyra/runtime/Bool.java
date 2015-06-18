package lyra.runtime;

/**
 *
 */
public class Bool extends Object {
    private boolean value = false;

    public static Bool _true = new Bool(true);
    public static Bool _false = new Bool(false);

    private Bool(boolean value) {
        this.value = value;
    }

    static public Bool getInstance(boolean value) {
        return value ? _true : _false;
    }

    public Bool lyra___not() {
        return new Bool(!value);
    }
    public Bool lyra___equals(Bool rhs) {
        return new Bool(value == rhs.value);
    }
    public Bool lyra___notequals(Bool rhs) {
        return new Bool(value != rhs.value);
    }
    public Bool lyra___and(Bool rhs) {
        return new Bool(value && rhs.value);
    }
    public Bool lyra___or(Bool rhs) {
        return new Bool(value || rhs.value);
    }

    public boolean valueOf() {
        return value;
    }
}
