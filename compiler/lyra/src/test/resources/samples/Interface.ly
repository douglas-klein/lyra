interface ArtsyObject {
    def interfaceMethod1 : Int;
    def interfaceMethod2(x : Int) : Int;
    def interfaceMethod3;
}


class ConcreteObject implements ArtsyObject {
    def interfaceMethod1 : Int {
        return 1;
    }
    def interfaceMethod2(x : Int) : Int {
        return 2;
    }
    def interfaceMethod3 {
        out.print("method3");
    }
}

class Application {
    def main {
        ArtsyObject o = new ConcreteObject();
        o.interfaceMethod1;
        o.interfaceMethod2(2);
        o.interfaceMethod3;
    }
}
