class Application {
    def main {
        method1 2;
	method2 "abc" "def";
        method1 (method2 5 (method1 "HUEHUE")) + 2;
	method1(3); //ambiguo
    }
    
    def method1(Int x) {
    }
    def method2(Int x, Int y) : Int {
    }
}
