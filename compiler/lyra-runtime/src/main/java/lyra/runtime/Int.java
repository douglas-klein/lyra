package lyra.runtime;

/**
 *
 */
public class Int extends Number {
    private int value;

    public Int(int value) {
        super(value);
        this.value = value;
    }

    public int valueOf() {
        return value;
    }

    public Int lyra___inc() { return new Int(value + 1); }
    public Int lyra___dec() { return new Int(value - 1); }
    public Bool lyra___not() { return Bool.getInstance(value == 0.0); }
    public Int lyra___positive() { return new Int(value); }
    public Int lyra___negative() { return new Int(-value); }
    public Int lyra___multiplied(Int rhs) {
        return new Int(value * rhs.value);
    }
    public Int lyra___divided(Int rhs) {
        return new Int(value / rhs.value);
    }
    public Int lyra___remainder(Int rhs) {
        return new Int(value % rhs.value);
    }
    public Int lyra___added(Int rhs) {
        return new Int(value + rhs.value);
    }
    public Int lyra___subtracted(Int rhs) {
        return new Int(value - rhs.value);
    }
    public Bool lyra___less(Int rhs) {
        return Bool.getInstance(value < rhs.value);
    }
    public Bool lyra___lessorequal(Int rhs) {
        return Bool.getInstance(value <= rhs.value);
    }
    public Bool lyra___equals(Int rhs) {
        return Bool.getInstance(value == rhs.value);
    }
    public Bool lyra___notequals(Int rhs) {
        return Bool.getInstance(value != rhs.value);
    }
    public Bool lyra___greaterorequals(Int rhs) {
        return Bool.getInstance(value >= rhs.value);
    }
    public Bool lyra___greater(Int rhs) {
        return Bool.getInstance(value > rhs.value);
    }
}
