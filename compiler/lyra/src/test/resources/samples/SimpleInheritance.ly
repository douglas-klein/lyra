class Parent {
    def parentMethod {
        out.writeln("Hello from parent");
    }
}

class Child extends Parent {
    def childMethod {
        out.writeln("Hello from child");
    }
}

class Application {
    def main {
        Child c = new Child();
        c.parentMethod;
        c.childMethod;
    }
}
