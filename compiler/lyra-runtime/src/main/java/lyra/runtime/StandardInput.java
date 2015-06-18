package lyra.runtime;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 */
public class StandardInput extends Input {
    public StandardInput() {
        super(System.in);
    }

    @Override
    public Bool lyra_open(String filename) {
        /* standard input is always open */
        return Bool._true;
    }

    @Override
    public Void lyra_close() {
        /* standard input never closes */
        return Void._void;
    }

}
