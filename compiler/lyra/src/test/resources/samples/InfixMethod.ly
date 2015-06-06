class A {
    def op(rhs : A) : A {}
    def __added(rhs: A ) : A {}
    def __multiplied(rhs : A ) : A {}
}

class Application {
    def main {
        A a = new A();
        A b = new A();
        A c = new A();
        A c = a op b + c; // ~= a op (b + c)
    }
}
