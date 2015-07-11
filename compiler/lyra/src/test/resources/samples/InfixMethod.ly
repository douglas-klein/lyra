class A {
    def op(rhs : A) : A { return new A(); }
    def __added(rhs: A ) : A { return new A(); }
    def __multiplied(rhs : A ) : A { return new A(); }
}

class Application {
    def main {
        A a = new A();
        A b = new A();
        A c = new A();
        A d = a op b + c; // ~= a op (b + c)
    }
}
