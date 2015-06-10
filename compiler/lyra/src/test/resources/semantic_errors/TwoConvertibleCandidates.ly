class A {
    def constructor(x : C) { }
}

class B {
    def constructor(x : C) { }
}

class C {

}

class T {
    def m(a : A) {}
    def m(b : B) {}
}

class Application {
    def main {
        T t = new T();
        C c = new C();
        t.m(c);
    }
}