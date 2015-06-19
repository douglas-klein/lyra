package lyra.runtime;

import java.io.*;

/**
 *
 */
public class Output extends Object {
    Writer writer;
    Bool errorFlag = Bool._false;

    protected Output(OutputStream stream) {
        if (stream != null) {
            writer = new OutputStreamWriter(stream);
        }
    }
    public Output() {
        this(null);
    }

    public Bool lyra_open(String filename) {
        if (lyra_isOpen().valueOf())
            return Bool._false;

        try {
            writer = new BufferedWriter(new FileWriter(filename.valueOf()));
        } catch (IOException e) {
            errorFlag = Bool._true;
            return Bool._false;
        }

        return Bool._true;
    }

    public Void lyra_close() {
        try {
            writer.close();
        } catch (IOException e) {
        } finally {
            writer = null;
        }

        return Void._void;
    }

    public Bool lyra_isOpen() {
        return Bool.getInstance(writer != null);
    }

    public Void lyra_flush() {
        try {
            writer.flush();
        } catch (IOException e) {
        }

        return Void._void;
    }

    public Bool lyra_hasError() {
        return errorFlag;
    }

    public Void lyra_write(String text) {
        try {
            writer.write(text.valueOf());
        } catch (IOException e) {
        }

        return Void._void;
    }

    public Void lyra_write(Object obj) {
        return lyra_write(obj.lyra_toString());
    }

    public Void lyra_writeln(String text) {
        Void v = lyra_write(new String(text.valueOf() + "\n"));
        lyra_flush();
        return v;
    }
    public Void lyra_writeln(Object obj) {
        Void v = lyra_writeln(obj.lyra_toString());
        lyra_flush();
        return v;
    }
}
