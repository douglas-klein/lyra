class Application {
    def main {
        Int i = 0;
        forever {
            out.writeln(i);
            i++;
            if i == 2 {
                break;
            }
        }
    }
}