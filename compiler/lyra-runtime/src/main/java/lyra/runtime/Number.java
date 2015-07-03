package lyra.runtime;

/**
 *
 */
public class Number extends Object {
    private double value;

    public Number(double value) {
        this.value = value;
    }

    public Number lyra___inc() { return new Number(value + 1); }
    public Number lyra___dec() { return new Number(value - 1); }
    public Bool lyra___not() { return Bool.getInstance(value == 0.0); }
    public Number lyra___positive() { return new Number(value); }
    public Number lyra___negative() { return new Number(-value); }
    public Number lyra___multiplied(Number rhs) {
        return new Number(value * rhs.value);
    }
    public Number lyra___divided(Number rhs) {
        return new Number(value / rhs.value);
    }
    public Number lyra___remainder(Number rhs) {
        double result = value / rhs.value;
        result -= Math.floor(result);
        return new Number(result);
    }
    public Number lyra___added(Number rhs) {
        return new Number(value + rhs.value);
    }
    public Number lyra___subtracted(Number rhs) {
        return new Number(value - rhs.value);
    }
    public Bool lyra___less(Number rhs) {
        return Bool.getInstance(value < rhs.value);
    }
    public Bool lyra___lessorequal(Number rhs) {
        return Bool.getInstance(value <= rhs.value);
    }
    public Bool lyra___equals(Number rhs) {
        return Bool.getInstance(value == rhs.value);
    }
    public Bool lyra___notequals(Number rhs) {
        return Bool.getInstance(value != rhs.value);
    }
    public Bool lyra___greaterorequals(Number rhs) {
        return Bool.getInstance(value >= rhs.value);
    }
    public Bool lyra___greater(Number rhs) {
        return Bool.getInstance(value > rhs.value);
    }
}
