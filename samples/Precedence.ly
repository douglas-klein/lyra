class Application {
    def main {
        Int a = 1 + 1 * 2; //1 + (1 * 2)
        Int b = true and false or true; // (true and false) or true
        Bool c = 1 > 2 == 2 < 1; //(1 > 2) == (2 < 1)
        Int d = 1 + - 2; // 1 + (- 2) lexer bugado n�o aceita -2
        Bool e = 1 > 2 == ! false; // (1 > 2) == (! false)
        Int f = -a++;
    }
}
