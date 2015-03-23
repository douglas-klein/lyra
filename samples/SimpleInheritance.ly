class Parent {
    def parentMethod {
        out.print("Hello from parent");
    }
}

class Child extends Parent {
    def childMethod {
        out.print("Hello from child");
    }
}

class Application {
    def main {
        Child c = new Child();
        c.parentMethod;
        c.childMethod;
    }
}
