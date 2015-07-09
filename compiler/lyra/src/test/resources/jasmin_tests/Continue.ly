class Application {
    def main {
        for Int i = 0; i < 3; i++ {
            if (i == 1) {
                continue;
            }
            out.writeln(i);
        }
    }
}