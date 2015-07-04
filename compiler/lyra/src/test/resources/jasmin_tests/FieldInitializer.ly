class A {
    Int x = 2;
    Int y;

    def constructor {
        out.writeln(x);
        x = 3;
        y = 4;
    }
}

class B {
    Int z = 5;
}

class Application {
    def main {
        A a = new A();
        out.writeln(a.x);
        out.writeln(a.y);

        B b = new B();
        out.writeln(b.z);
    }
}