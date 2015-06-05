package lyra.listeners;

import lyra.LyraLexer;
import lyra.LyraParser;
import lyra.LyraParserBaseListener;
import lyra.symbols.*;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.HashSet;
import java.util.Iterator;

/**
 * This listener locates avery occurrence of arrays of any dimension count and rewrites the
 * references to the array to type as references to a generated type. Use of the subscript
 * operator is also rewritten into methods of the generated class.
 *
 * For instance, suppose X is a type (class, enum or interfaces):
 * - X[] becomes X$Array,
 * - X[][] becomes X$Array$Array
 * - ... and so on
 *
 * Usage of the [] operator on instances of an array is rewriten into calling the method __at().
 * If that access is used on the left side of an assignment, then the whole assignment will be
 * rewritten in terms of the __set() method.
 *
 * [] operator is rewritten for any usage, even outside of arrays, this allows user-defined
 * classes to provide random-access to data using the syntax for arrays. Obviously the generated
 * $Array classes provide cleaner methods called set() and at() that are equivalent to their
 * underlined counterparts.
 */
public class ArrayRewriterListener extends TreeRewriterBaseListener {
    /**
     * If a particular MemberFactorContext instance is in this set, then it is a falsification
     * inserted into the parse tree by exitArrayFactor. It is a rewrite of
     *   factor '[' expr ']'
     * into
     *   factor.__at(expr)
     * .
     */
    private HashSet<LyraParser.MemberFactorContext> rewrittenArrayAcess = new HashSet<>();

    @Override
    public void exitArrayFactor(LyraParser.ArrayFactorContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        LyraParser.MemberFactorContext rewritten = new LyraParser.MemberFactorContext(
                new LyraParser.FactorContext(parent, -1));

        LyraParser.FactorContext object = ctx.factor();
        object.parent = rewritten;
        rewritten.addChild(object);

        rewritten.addChild(new CommonToken(LyraLexer.DOT, "."));
        rewritten.addChild(new CommonToken(LyraLexer.IDENT, "__at"));
        rewritten.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));

        LyraParser.ArgsContext args = new LyraParser.ArgsContext(rewritten, -1);
        LyraParser.ExprContext expr = ctx.expr();
        expr.parent = args;
        args.addChild(expr);
        rewritten.addChild(args);

        rewritten.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));

        replaceChild(ctx, parent, rewritten);
        rewrittenArrayAcess.add(rewritten);
    }

    @Override
    public void exitType(LyraParser.TypeContext ctx) {
        int dimensions = ctx.arrayDeclSuffix().size();
        if (dimensions == 0)
            return; // not an array

        String elementTypeName = ctx.IDENT().getText();
        String arrayName = ArrayClassFactory.getArrayTypeName(elementTypeName, dimensions);

        LyraParser.TypeContext rewritten = new LyraParser.TypeContext(ctx.getParent(), -1);
        rewritten.addChild(new CommonToken(LyraLexer.IDENT, arrayName));

        replaceChild(ctx, ctx.getParent(), rewritten);
    }

    @Override
    public void exitArrayAlocExpr(LyraParser.ArrayAlocExprContext ctx) {
        int dimensions = ctx.LEFTBRACKET().size();
        if (dimensions == 0) return;

        String elementTypeName = ctx.IDENT().getText();
        String arrayName = ArrayClassFactory.getArrayTypeName(elementTypeName, dimensions);

        LyraParser.ObjectAlocExprContext rewritten = new LyraParser.ObjectAlocExprContext(
                new LyraParser.AlocExprContext(ctx.getParent(), -1));
        rewritten.addChild(new CommonToken(LyraLexer.NEW, "new"));
        rewritten.addChild(new CommonToken(LyraLexer.IDENT, arrayName));
        rewritten.addChild(new CommonToken(LyraLexer.LEFTPARENTHESES, "("));

        LyraParser.ArgsContext args = new LyraParser.ArgsContext(rewritten, -1);
        for(Iterator<LyraParser.ExprContext> it = ctx.expr().iterator(); it.hasNext();) {
            LyraParser.ExprContext expr = it.next();
            expr.parent = args;
            args.addChild(expr);
            if (it.hasNext())
                args.addChild(new CommonToken(LyraLexer.COMMA, ","));
        }
        rewritten.addChild(args);

        rewritten.addChild(new CommonToken(LyraLexer.RIGHTPARENTHESES, ")"));

        replaceChild(ctx, ctx.getParent(), rewritten);
    }

    @Override
    public void exitExpr(LyraParser.ExprContext ctx) {
        if (ctx.unaryexpr() != null)
            return; //handled at exitUnaryexpr
        if (ctx.binOp == null)
            return;
        if (ctx.binOp.getType() != LyraLexer.EQUALOP)
            return;

        /* we have an assignment expression */
        if (ctx.expr(0).unaryexpr() == null)
            return; //semantic error: assigning anonymous reference
        LyraParser.FactorContext factor = ctx.expr(0).unaryexpr().factor();
        if (factor == null) return;
        if (!(factor instanceof LyraParser.MemberFactorContext)) return;

        LyraParser.MemberFactorContext left = (LyraParser.MemberFactorContext) factor;
        if (!rewrittenArrayAcess.contains(left))
            return; //not a array access rewrite

        /* we have an assignment expression where the left side is an [] operator usage */

        /* detach right from ctx */
        LyraParser.ExprContext right = ctx.expr(1);
        right.parent = left.args();
        ctx.children.remove(right);

        /* replace "__at()" in left with "__set(" + right + ")" */
        TerminalNodeImpl set = new TerminalNodeImpl(new CommonToken(LyraLexer.IDENT, "__set"));
        set.parent = left;
        replaceChild(left.IDENT(), left, set);
        left.args().addChild(right);

        /* rewrite whole expr as modified expr */
        LyraParser.ExprContext rewritten = new LyraParser.ExprContext(ctx.getParent(), -1);
        LyraParser.UnaryexprContext uExpr = new LyraParser.UnaryexprContext(rewritten, -1);
        left.parent = uExpr;
        uExpr.addChild(left);
        rewritten.addChild(uExpr);

        rewrittenArrayAcess.remove(left); //or mabe not?
        replaceChild(ctx, ctx.getParent(), rewritten);
    }
}
