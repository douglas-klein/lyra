package lyra.symbols;

/**
 * Values for the public, private, protected keywords
 */
public enum Visibility {
    PRIVATE("private"),
    PROTECTED("protected"),
    PUBLIC("public");

    private String name;

    Visibility(String name) {
        this.name = name;
    }

    static public Visibility fromName(String name) {
        if (name.equals(PRIVATE.toString()) )  return PRIVATE;
        if (name.equals(PROTECTED.toString())) return PROTECTED;
        if (name.equals(PUBLIC.toString()))    return PUBLIC;
        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
