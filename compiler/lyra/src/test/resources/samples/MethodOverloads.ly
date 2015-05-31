class B {
    def inheritedSpec(x : Int) {}
}

class U {

}
class T {
    def constructor(u : U) {}
}

class A extends B {
    def method1(x : Number) {}
    def method2(x : Number) {}
    def method2(x : Int) {}
    def inheritedSpec(x: Number) {}
    def method3(x : Int, y : Number) {}
    def method3(x : Int, y : Int) {}

    def method4(x : Number, y : Number) {}
    def method4(x : Number, y : T) {}
}

class Application {
    def main {
        out.write("oba oba");
    }
}