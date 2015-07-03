class Application {
    def main {
        String[][] arr = new String[2][3];

        arr[0][0] = "0x0";
        arr[0][1] = "0x1";
        arr[0][2] = "0x2";
        arr[1][0] = "1x0";
        arr[1][1] = "1x1";
        arr[1][2] = "1x2";

        out.writeln(arr[0][0]);
        out.writeln(arr[0][1]);
        out.writeln(arr[0][2]);
        out.writeln(arr[1][0]);
        out.writeln(arr[1][1]);
        out.writeln(arr[1][2]);
    }
}