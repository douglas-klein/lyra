class Square {
    Int height = 0;
    Int width = 0;

    def constructor(height : Int, width : Int) {
        super();
        this.height = height;
        this.width = width;
    }
}

class Application {
    def main {
        Square a = new Square(2, 3);
        out.writeln(a.height);
        out.writeln(a.width);
    }
}