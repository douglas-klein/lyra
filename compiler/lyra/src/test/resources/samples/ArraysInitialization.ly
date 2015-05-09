class Value {}

class Application {
    Int line[] = new Int[48];
    Int square[][] = new Int[2][3];

    Value cube[][][] = new Value[3][2][1];

    def main {
        Int x = 2;
        Int y = 3;
        Int z = 4;
        Int myLine[] = new Int[x];
        Int mySquare[][] = new Int[3][z];

        Value myCube[][][] = new Value[x][y][z];
    }
}