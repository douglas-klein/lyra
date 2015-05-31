class A {
    def method1(x : Number) { out.write("method1(Number)"); }
    def method2(x : Number) { out.write("method2(Number)"); }
    def method2(x : Int) { out.write("method2(Int)"); }
    def method3(x : Int, y : Number) { out.write("method3(Int, Number)"); }
    def method3(x : Int, y : Int) { out.write("method3(Int, Int)"); }
}

class Application {
    def main {
        out.write("oba oba");
    }
}