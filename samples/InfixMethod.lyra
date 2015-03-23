class Object {
    def op(Object rhs) : Object {}
    def __add(Object rhs) : Object {}
    def __mul(Object rhs) : Object {}
}

class Application {
    def main {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object c = a op b + c; // ~= a op (b + c)
    }
}
