package lyra.runtime;


/**
 *
 */
public class String extends Object {
    private java.lang.String value;
    static java.lang.String trueValues[] = {
            "true", "1", "True", "TRUE", "on"
    };
    static java.lang.String falseValues[] = {
            "false", "0", "False", "FALSE", "off"
    };

    public String(java.lang.String string) {
        value = string;
    }

    public java.lang.String valueOf() {
        return value;
    }

    public Int lyra_length() {
        return new Int(value.length());
    }
    public String lyra___at(Int idx) {
        return new String(value.substring(idx.valueOf(), idx.valueOf()+1));
    }
    public Void lyra___set(Int idx, String singleChar) {
        int idxVal = idx.valueOf();
        value = value.substring(0, idxVal)
                + singleChar
                + value.substring(idxVal+1);
        return Void._void;
    }
    public String lyra_at(Int idx) {
        return lyra___at(idx);
    }
    public Void lyra_set(Int idx, String singleChar) {
        return lyra___set(idx, singleChar);
    }

    public Bool lyra___less(String rhs) {
        return Bool.getInstance(value.compareTo(rhs.value) < 0);
    }
    public Bool lyra___lessorequal(String rhs) {
        return Bool.getInstance(value.compareTo(rhs.value) <= 0);
    }
    public Bool lyra___equals(String rhs) {
        return Bool.getInstance(value.compareTo(rhs.value) == 0);
    }
    public Bool lyra___notequals(String rhs) {
        return Bool.getInstance(value.compareTo(rhs.value) != 0);
    }
    public Bool lyra___greaterorequal(String rhs) {
        return Bool.getInstance(value.compareTo(rhs.value) >= 0);
    }
    public Bool lyra___greater(String rhs) {
        return Bool.getInstance(value.compareTo(rhs.value) > 0);
    }

    public Int lyra_parseInt() {
        int iVal;
        try {
            iVal = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
        return new Int(iVal);
    }

    public Number lyra_parseNumber() {
        double dVal;
        try {
            dVal = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
        return new Number(dVal);
    }

    public Bool lyra_parseBool() {
        for (int i = 0; i < trueValues.length; ++i)
            if (trueValues[i].equals(value))
                return Bool._true;
        for (int i = 0; i < falseValues.length; ++i)
            if (falseValues[i].equals(value))
                return Bool._false;
        return null;
    }

}
