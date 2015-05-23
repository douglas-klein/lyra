class Value {
    Int value;

    def constructor(value : Int) {
        this.value = value;
    }
}

class Application {
    Int a, b = 1, 2;

    def main {
        Int x, y, z = 1, 2, 3;
        Value i, j = new Value(x), new Value(2);
        Int k, h = x, y;
    }
}