package lyra.listeners;

import jdk.nashorn.internal.ir.Terminal;
import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParserBaseListener;
import lyra.tokens.NumberToken;
import lyra.tokens.StringToken;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ListIterator;

/**
 * Common code between all listeners that rewrite the parse tree before the semantic analysis.
 */
public class TreeRewriterBaseListener extends LyraParserBaseListener {
    protected static void replaceChild(ParseTree victim, ParserRuleContext parent,
                                       ParseTree replacement) {
        int line = getNodeLine(victim);
        if (line > 0)
            updateNodeTokensLine(replacement, line);

        final ListIterator<ParseTree> iterator = parent.children.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next() == victim)
                iterator.set(replacement);
        }
    }

    private static void updateNodeTokensLine(ParseTree node, int line) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (!(child instanceof TerminalNode)) {
                updateNodeTokensLine(child, line);
            } else {
                Token symbol = ((TerminalNode) child).getSymbol();
                if (!(symbol instanceof CommonToken)) continue;

                CommonToken token = (CommonToken) symbol;
                if (token.getLine() <= 0)
                    token.setLine(line);
            }
        }
    }

    private static int getNodeLine(ParseTree node) {
        int line = 0;

        for (int i = 0; line <= 0 && i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof TerminalNode) {
                line = ((TerminalNode) child).getSymbol().getLine();
            } else  {
                line = getNodeLine(child);
            }
        }
        return line;
    }

    protected LyraParser.ExprContext wrapExpression(ParserRuleContext parent,
                                                    LyraParser.ExprContext expr) {
        LyraParser.ExprContext wrapped = new LyraParser.ExprContext(parent, -1);
        LyraParser.UnaryexprContext uExpr = new LyraParser.UnaryexprContext(wrapped, -1);

        uExpr.addChild(wrapExpressionIntoFactor(uExpr, expr));
        wrapped.addChild(uExpr);

        return wrapped;
    }

    protected LyraParser.FactorContext wrapExpressionIntoFactor(ParserRuleContext parent,
                                                                LyraParser.ExprContext expr) {
        LyraParser.WrappedFactorContext factor = new LyraParser.WrappedFactorContext(
                new LyraParser.FactorContext(parent, -1));
        factor.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
        expr.parent = factor;
        factor.addChild(expr);
        factor.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));
        return factor;
    }

    protected LyraParser.ExprContext createNameFactorExpression(ParserRuleContext parent,
                                                                String name) {
        LyraParser.NameFactorContext factor = new LyraParser.NameFactorContext(
                new LyraParser.FactorContext(null, -1));
        factor.addChild(new CommonToken(LyraLexer.IDENT, name));
        return createExprForFactor(parent, factor);
    }

    protected LyraParser.ExprContext createFieldExpression(ParserRuleContext parent,
                                                           String objName, String fieldName) {
        LyraParser.MemberFactorContext factor = new LyraParser.MemberFactorContext(
                new LyraParser.FactorContext(null, -1));

        LyraParser.NameFactorContext left = new LyraParser.NameFactorContext(
                new LyraParser.FactorContext(factor, -1));
        left.addChild(new CommonToken(LyraLexer.IDENT, objName));
        factor.addChild(left);

        factor.addChild(new CommonToken(LyraLexer.DOT, "."));
        factor.addChild(new CommonToken(LyraLexer.IDENT, fieldName));
        return createExprForFactor(parent, factor);
    }

    protected LyraParser.ExprContext createMethodExpr(ParserRuleContext parent,
                                                      LyraParser.FactorContext left,
                                                      String methodName,
                                                      LyraParser.ExprContext... args) {
        LyraParser.MemberFactorContext factor = new LyraParser.MemberFactorContext(
                new LyraParser.FactorContext(null, -1));

        factor.addChild(left);
        factor.addChild(new CommonToken(LyraLexer.DOT, "."));
        factor.addChild(new CommonToken(LyraLexer.IDENT, methodName));
        if (args.length > 0) {
            factor.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
            LyraParser.ArgsContext argsCtx = new LyraParser.ArgsContext(factor, -1);
            for (int i = 0; i < args.length; i++) {
                if (i > 0)
                    argsCtx.addChild(new CommonToken(LyraLexer.COMMA, ","));

                argsCtx.addChild(args[i]);
            }
            factor.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));
        }

        return createExprForFactor(parent, factor);
    }

    protected LyraParser.ExprContext createExprForFactor(ParserRuleContext parent,
                                                         LyraParser.FactorContext factor) {
        LyraParser.ExprContext expr = new LyraParser.ExprContext(parent, -1);
        LyraParser.UnaryexprContext uexpr = new LyraParser.UnaryexprContext(expr, -1);
        factor.parent = uexpr;
        uexpr.addChild(factor);
        expr.addChild(uexpr);
        return expr;
    }

    protected LyraParser.VarDeclContext createSimpleVarDecl(ParserRuleContext parent,
                                                            String typeName, String varName,
                                                            String literalInitializer,
                                                            int literalInitializerTokenType) {
        LyraParser.FactorContext factor = null;
        if (literalInitializer != null) {
            if (literalInitializerTokenType == LyraLexer.NUMBER) {
                factor = new  LyraParser.NumberFactorContext(
                        new LyraParser.FactorContext(null, -1));
                factor.addChild(new NumberToken(literalInitializerTokenType, literalInitializer));
            } else if (literalInitializerTokenType == LyraLexer.STRING) {
                factor = new LyraParser.StringFactorContext(
                        new LyraParser.FactorContext(null, -1));
                factor.addChild(new StringToken(literalInitializerTokenType, literalInitializer));
            } else {
                throw new RuntimeException("Bad literalInitializerTokenType ("
                        + literalInitializerTokenType + ").");
            }

        }
        return createSimpleVarDecl(parent, typeName, varName, factor);
    }

    protected LyraParser.VarDeclContext createSimpleVarDecl(ParserRuleContext parent,
                                                            String typeName, String varName,
                                                            LyraParser.FactorContext initializer) {
        LyraParser.VarDeclContext var = new LyraParser.VarDeclContext(parent, -1);
        LyraParser.TypeContext type = new LyraParser.TypeContext(var, -1);
        type.addChild(new CommonToken(LyraLexer.IDENT, typeName));
        var.addChild(type);

        LyraParser.VarDeclUnitContext unit = new LyraParser.VarDeclUnitContext(var, -1);
        unit.addChild(new CommonToken(LyraLexer.IDENT, varName));
        if (initializer != null) {
            unit.addChild(new CommonToken(LyraLexer.EQUALOP, "="));
            unit.addChild(createExprForFactor(unit, initializer));
        }
        var.addChild(unit);
        return var;
    }


    protected LyraParser.MethodDeclContext createMethod(ParserRuleContext parent, String visibility,
                                                      boolean infix, String name,
                                                      String returnType, String... paramStrings) {
        LyraParser.MethodDeclContext m = new LyraParser.MethodDeclContext(parent, -1);
        m.addChild(new CommonToken(LyraLexer.VISIBILITYMODIFIER, visibility));
        m.addChild(new CommonToken(LyraLexer.DEF, "def"));
        if (infix)
            m.addChild(new CommonToken(LyraLexer.INFIX, "infix"));
        m.addChild(new CommonToken(LyraLexer.IDENT, name));

        if (paramStrings.length > 0) {
            m.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
            LyraParser.ParamsContext params = new LyraParser.ParamsContext(m, -1);

            for (int i = 0; i < paramStrings.length; ) {
                if (i > 0)
                    params.addChild(new CommonToken(LyraLexer.COMMA, ","));

                String pName = paramStrings[i++];
                String pType = paramStrings[i++];

                LyraParser.ParamDeclContext param = new LyraParser.ParamDeclContext(params, -1);
                param.addChild(new CommonToken(LyraLexer.IDENT, pName));
                param.addChild(new CommonToken(LyraLexer.COLON, ":"));

                LyraParser.TypeContext type = new LyraParser.TypeContext(param, -1);
                type.addChild(new CommonToken(LyraLexer.IDENT, pType));
                param.addChild(type);
                params.addChild(param);
            }

            m.addChild(params);
            m.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));
        }

        m.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));
        m.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));
        m.addChild(new CommonToken(LyraLexer.COLON, ":"));
        LyraParser.TypeContext type = new LyraParser.TypeContext(m, -1);
        type.addChild(new CommonToken(LyraLexer.IDENT, returnType));
        m.addChild(type);

        m.addChild(new CommonToken(LyraLexer.LEFTCURLYBRACE, "{"));
        LyraParser.MethodBodyContext methodBody = new LyraParser.MethodBodyContext(m, -1);

        m.addChild(methodBody);
        m.addChild(new CommonToken(LyraLexer.RIGHTCURLYBRACE, "}"));
        return m;
    }

    protected String getPostfixOperatorMethod(Token token) {
        String method = null;
        switch (token.getType()) {
            case LyraLexer.INCREMENT:
                method = "__inc";
                break;
            case LyraLexer.DECREMENT:
                method = "__dec";
                break;
        }
        return method;
    }

    protected String getPrefixOperatorMethod(Token token) {
        String method = null;
        switch (token.getType()) {
            case LyraLexer.NOT:
                method = "__not";
                break;
            case LyraLexer.PLUS:
                method = "__positive";
                break;
            case LyraLexer.MINUS:
                method = "__negative";
                break;
        }
        return method;
    }

    protected String getBinaryOperatorMethod(Token token) {
        String methodName = null;
        switch (token.getType()) {
            case LyraLexer.MULTOP:
                methodName = "__multiplied";
                break;
            case LyraLexer.SLASH:
                methodName = "__divided";
                break;
            case LyraLexer.MODOP:
                methodName = "__remainder";
                break;
            case LyraLexer.PLUS:
                methodName = "__added";
                break;
            case LyraLexer.MINUS:
                methodName = "__subtracted";
                break;
            case LyraLexer.LESSTHAN:
                methodName = "__less";
                break;
            case LyraLexer.LESSTHANOREQUAL:
                methodName = "__lessorequal";
                break;
            case LyraLexer.MORETHANOREQUAL:
                methodName = "__greaterorequal";
                break;
            case LyraLexer.MORETHAN:
                methodName = "__greater";
                break;
            case LyraLexer.DOUBLEEQUALOP:
                methodName = "__equals";
                break;
            case LyraLexer.IS:
                methodName = "__is";
                break;
            case LyraLexer.NOTEQUAL:
                methodName = "__notequals";
                break;
            case LyraLexer.AND:
                methodName = "__and";
                break;
            case LyraLexer.OR:
                methodName = "__or";
                break;
            case LyraLexer.IDENT:
                methodName = token.getText();
                break;
        }
        return methodName;
    }
}
