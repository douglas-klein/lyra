package lyra.jasmin;

import lyra.LyraParser;
import lyra.symbols.*;
import org.antlr.v4.runtime.ParserRuleContext;

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
        return  "L" + type.getBinaryName() + ";";
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
        classSymbol.getInterfaces()
                .forEach(i -> writer.println(".implements " + i.getBinaryName()));
        writer.printf("\n\n");
    }

    public static void writeInterfacePrelude(PrintWriter writer, InterfaceSymbol interfaceSymbol) {
        writer.println(".source " + interfaceSymbol.getName() + ".j");
        writer.println(".interface public abstract " + interfaceSymbol.getBinaryName());
        writer.println(".super java/lang/Object");
        interfaceSymbol.getSuperInterfaces()
                .forEach(s -> writer.println(".implements " + s.getBinaryName()));
        writer.printf("\n\n");
    }


    public static boolean isPostfixIncDec(MethodSymbol method) {
        return method.isInfix() && method.getArguments().size() == 0
                && (method.getName().equals("__inc") || method.getName().equals("__dec"));
    }

    public static ParserRuleContext getBreakTarget(ParserRuleContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        while (parent != null
                && !(parent instanceof LyraParser.SwitchstatContext)
                && !(parent instanceof LyraParser.ForstatContext)) {
            parent = parent.getParent();
        }
        return parent;
    }

    public static LyraParser.ForstatContext getContinueTargetFor(ParserRuleContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        while (parent != null && !(parent instanceof LyraParser.ForstatContext))
            parent = parent.getParent();
        return parent == null ? null : (LyraParser.ForstatContext)parent;
    }
}
