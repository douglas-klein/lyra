class Application {
    def main {
        String[] arr = new String[3];
        arr[0] = "One";
        arr.set(1, "Two");
        arr[2] = "Three";

        out.writeln(arr[0]);
        out.writeln(arr.at(1));
        out.writeln(arr.at(2));
    }
}