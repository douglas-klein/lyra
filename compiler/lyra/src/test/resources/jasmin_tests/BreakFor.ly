class Application {
    def main {
        for Int i = 0; i < 100; i++ {
            out.writeln(i);
            if (i == 2) {
                break;
            }
        }
    }
}