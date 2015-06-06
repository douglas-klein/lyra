class A {
    Int value;

    def constructor(value : Int) {
        this.value = value;
    }
    def infix op(rhs: A) : A {
        return new A(value + rhs.value);
    }
}

class Application {
    def main {
        Int a = 1 + 2 + 3; //(1 + 2) + 3
        Int b = 1 * 2 * 3; //(1 * 2) * 3
        A c = new A(1) op new A(2) op new A(3); //(1 op 2) op 3
        Int d = 1 + 2 * 3; //1 + (2 * 3)
        Bool e = true == false == true != false; //((true == false) == true) != false
    }
}