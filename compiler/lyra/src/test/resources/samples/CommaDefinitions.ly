class Value {
    Int value;

    def constructor(value : Int) {
        this.value = value;
    }
}

class Application {
    Int a = 1, b = 2;

    def main {
        Int x = 1, y = 2, z = 3;
        Value i = new Value(x), j = new Value(2);
        Int k = x, h = y;
    }
}