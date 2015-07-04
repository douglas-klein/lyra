class Application {
    def main {
        out.writeln(this.m);
        out.writeln(this.premature);
    }

    def m : Int {
        return 24 - 1;
    }

    def premature : Int {
        Bool truth = false;
        if (!truth) {
            return 7;
        }
        return 6;
    }
}