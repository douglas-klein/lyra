package lyra.runtime;

import java.io.*;
import java.util.stream.IntStream;

/**
 *
 */
public class Input extends Object {
    private BufferedReader reader;
    private Bool errorFlag = Bool._false;
    private Bool endFlag = Bool._false;

    protected Input(InputStream stream) {
        if (stream != null) {
            reader = new BufferedReader(new InputStreamReader(stream));
        }
    }
    public Input() {
        this(null);
    }

    private void clear() {
        endFlag = Bool._false;
        errorFlag = Bool._false;
        reader = null;
    }


    public Bool lyra_open(String filename) {
        if (lyra_isOpen().valueOf())
            return Bool._false;

        clear();
        try {
            FileInputStream stream = new FileInputStream(filename.valueOf());
            reader = new BufferedReader(new InputStreamReader(stream));
        } catch (FileNotFoundException e) {
            errorFlag = Bool._true;
            return Bool._false;
        }

        return Bool._true;
    }

    public Void lyra_close() {
        try {
            reader.close();
        } catch (IOException e) {
        } finally {
            reader = null;
        }
        clear();

        return Void._void;
    }

    public Bool lyra_isOpen() { return Bool.getInstance(reader != null); }

    public Bool lyra_hasError() {
        return errorFlag;
    }

    public Bool lyra_atEnd() {
        return endFlag;
    }

    public String lyra_read(Int count) {
        try {
            char chars[] = new char[count.valueOf()];

            int result = reader.read(chars);
            if (result >= 1) {
                return new String(new java.lang.String(chars, 0, result));
            } else if (result == -1) {
                endFlag = Bool._true;
            } else {
                errorFlag = Bool._true;
            }
        } catch (IOException e) {
            errorFlag = Bool._true;
        }

        return null;
    }

    public String lyra_readLine() {
        try {
            return new String(reader.readLine());
        } catch (IOException e) {
            errorFlag = Bool._true;
        }

        return null;
    }

}
