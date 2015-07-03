package lyra.jasmin;

import lyra.symbols.*;

import java.io.PrintWriter;

/**
 * Generic Jasmin-related utilities
 */
public class Utils {

    public static String mapVisibility(Visibility visibility) {
        switch (visibility) {
            case PRIVATE: return "private";
            case PUBLIC: return "public";
            case PROTECTED: return "protected";
            default: break;
        }
        return null;
    }

    public static String typeSpec(TypeSymbol type) {
        String spec = "L";
        if (type instanceof InterfaceSymbol)
            spec = "I";
        spec += type.getBinaryName() + ";";
        return spec;
    }

    public static String methodSpec(MethodSymbol method) {
        String returnSpec = (method.isConstructor()) ? "V" : typeSpec(method.getReturnType());

        return String.format("%1$s/%2$s(%3$s)%4$s",
                ((TypeSymbol)method.getEnclosingScope()).getBinaryName(),
                method.getBinaryName(),
                method.getArgumentTypes().stream().map(t -> typeSpec(t))
                        .reduce((a, b) -> a + b).orElse(""),
                returnSpec);
    }

    public static void writeClassPrelude(PrintWriter writer, ClassSymbol classSymbol) {
        writer.println(".source " + classSymbol.getName() + ".j");
        writer.println(".class public " + classSymbol.getBinaryName());
        writer.println(".super " + classSymbol.getSuperClass().getBinaryName());
        writer.println();
        writer.println();
    }


}
