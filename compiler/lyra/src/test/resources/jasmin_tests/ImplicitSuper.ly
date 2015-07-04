class A {
    Int x;

    def constructor {
        x = 2;
    }
}

class B extends A {
    Int y;

    def constructor {
        //implicit super call
        y = 3;
    }
}

class Application {
    def main {
        B b = new B();
        out.writeln(b.x);
        out.writeln(b.y);
    }
}