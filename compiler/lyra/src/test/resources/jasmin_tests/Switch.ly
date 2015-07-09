class Application {
    def main {
        test(1);
        test(2);
        test(99);
    }

    def test(value : Int) {
        switch (value) {
            case 1:
                out.writeln(1);
            case 2:
                out.writeln(2);
            case default:
                out.writeln(3);
        }
    }
}