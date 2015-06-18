package lyra.runtime;

/**
 *
 */
public class StandardOutput extends Output {

    public StandardOutput() {
        super(System.out);
    }

    @Override
    public Bool lyra_open(String filename) {
        /* always open */
        return Bool._false;
    }

    @Override
    public Void lyra_close() {
        /* never closes */
        return Void._void;
    }

}
