class Object {
    def op(rhs : Object) : Object {}
    def __add(rhs: Object ) : Object {}
    def __mul(rhs : Object ) : Object {}
}

class Application {
    def main {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object c = a op b + c; // ~= a op (b + c)
    }
}
