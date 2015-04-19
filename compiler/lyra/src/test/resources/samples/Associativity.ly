class Application {
    def main {
        Int a = 1 + 2 + 3; //(1 + 2) + 3
        Int b = 1 * 2 * 3; //(1 * 2) * 3
        Object c = new Object(1) op new Object(2) op new Object(3); //(1 op 2) op 3
        Int d = 1 + 2 * 3; //1 + (2 * 3)
        Bool e = true == false == true != false; //((true == false) == true) != false
    }
}