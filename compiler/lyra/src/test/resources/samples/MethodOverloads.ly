class B {
    def inheritedSpec(x : Int) {}
}

class A extends B {
    def method1(x : Number) {}
    def method2(x : Number) {}
    def method2(x : Int) {}
    def inheritedSpec(x: Number) {}
    def method3(x : Int, y : Number) {}
    def method3(x : Int, y : Int) {}
}

class Application {
    def main {
        out.write("oba oba");
    }
}