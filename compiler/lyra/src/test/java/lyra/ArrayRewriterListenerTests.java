package lyra;

import lyra.LyraParser;
import lyra.listeners.ArrayRewriterListener;
import lyra.listeners.SyntacticSugarListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ArrayRewriterListener
 */
public class ArrayRewriterListenerTests {

    private InputStreamReader getReader(String name) {
        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream(name);
        InputStreamReader reader = new InputStreamReader(stream);
        return reader;
    }

    @Test
    public void testRewrite1DArrayType() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/Rewrite1DArrayAccess.ly");
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());
        walker.walk(new ArrayRewriterListener(), compiler.getParseTree());

        final boolean visited[] = {false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitVarDecl(lyra.LyraParser.VarDeclContext ctx) {
                if (!ctx.varDeclUnit(0).IDENT().getText().equals("arr"))
                    return;

                visited[0] = true;
                assertEquals(0, ctx.type().arrayDeclSuffix().size());
                assertEquals("Int$Array", ctx.type().IDENT().getText());
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
    }

    @Test
    public void testRewriteNAryArrayType() throws Exception {
        Compiler compiler = new Compiler();
        compiler.init(getReader("samples/ArraysDeclaration.ly"));
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());
        walker.walk(new ArrayRewriterListener(), compiler.getParseTree());

        final boolean visited[] = {false, false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitAttributeDecl(lyra.LyraParser.AttributeDeclContext ctx) {
                if (!ctx.varDecl().varDeclUnit(0).IDENT().getText().equals("cube"))
                    return;

                visited[0] = true;
                assertEquals(0, ctx.varDecl().type().arrayDeclSuffix().size());
                assertEquals("Value$Array$Array$Array", ctx.varDecl().type().IDENT().getText());
            }

            @Override
            public void exitVarDecl(lyra.LyraParser.VarDeclContext ctx) {
                if (!ctx.varDeclUnit(0).IDENT().getText().equals("cube"))
                    return;

                visited[1] = true;
                assertEquals(0, ctx.type().arrayDeclSuffix().size());
                assertEquals("Value$Array$Array$Array", ctx.type().IDENT().getText());
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
        assertTrue(visited[1]);
    }

    @Test
    public void testRewriteArrayAccess() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/Rewrite1DArrayAccess.ly");
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());
        walker.walk(new ArrayRewriterListener(), compiler.getParseTree());

        final boolean visited[] = {false, false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitVarDecl(lyra.LyraParser.VarDeclContext ctx) {
                if (!ctx.varDeclUnit(0).IDENT().getText().equals("x")) return;

                visited[0] = true;

                assertEquals(1, ctx.varDeclUnit().size());
                assertNotNull(ctx.varDeclUnit(0).expr());
                assertNotNull(ctx.varDeclUnit(0).expr().unaryexpr());
                lyra.LyraParser.FactorContext factor = ctx.varDeclUnit(0).expr().unaryexpr().factor();
                assertNotNull(factor);
                assertTrue(factor instanceof lyra.LyraParser.MemberFactorContext);

                lyra.LyraParser.MemberFactorContext member = (lyra.LyraParser.MemberFactorContext) factor;
                assertEquals("__at", member.IDENT().getText());
                assertEquals("arr", member.factor().getText());
                assertEquals(1, member.args().expr().size());
                assertTrue(Pattern.matches("\\(?0\\)?", member.args().expr(0).getText()));
            }

            @Override
            public void exitExpr(lyra.LyraParser.ExprContext ctx) {
                if (ctx.unaryexpr() == null) return;
                lyra.LyraParser.FactorContext factor = ctx.unaryexpr().factor();
                if (factor == null) return;
                if (!(factor instanceof lyra.LyraParser.MemberFactorContext)) return;

                lyra.LyraParser.MemberFactorContext member = (lyra.LyraParser.MemberFactorContext)factor;
                if (member.getText().indexOf("4640") < 0) return;

                visited[1] = true;

                assertEquals("arr", member.factor().getText());
                assertEquals("__set", member.IDENT().getText());

                lyra.LyraParser.ArgsContext args = member.args();
                assertEquals(2, args.expr().size());
                assertTrue(Pattern.matches("\\(?0\\)?", args.expr(0).getText()));
                assertTrue(Pattern.matches("\\(?4640\\)?", args.expr(1).getText()));
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
        assertTrue(visited[1]);
    }

    @Test
    public void testRewrite2DArrayAccess() throws Exception {
        Compiler compiler = new Compiler();
        compiler.init(getReader("samples/Rewrite2DArrayAccess.ly"));
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());
        walker.walk(new ArrayRewriterListener(), compiler.getParseTree());

        final boolean visited[] = {false, false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitVarDecl(lyra.LyraParser.VarDeclContext ctx) {
                if (!ctx.varDeclUnit(0).IDENT().getText().equals("x"))
                    return;

                visited[0] = true;
                assertNotNull(ctx.varDeclUnit(0).expr());
                LyraParser.FactorContext factor_ = ctx.varDeclUnit(0).expr().unaryexpr().factor();
                assertTrue(factor_ instanceof LyraParser.MemberFactorContext);
                LyraParser.MemberFactorContext factor = (LyraParser.MemberFactorContext) factor_;

                assertEquals("__at", factor.IDENT().getText());
                assertTrue(Pattern.matches("\\(?2\\)?", factor.args().getText()));

                assertTrue(factor.factor() instanceof LyraParser.MemberFactorContext);
                LyraParser.MemberFactorContext leftFactor =
                        (LyraParser.MemberFactorContext) factor.factor();
                assertEquals("__at", leftFactor.IDENT().getText());
                assertTrue(Pattern.matches("\\(?1\\)?", leftFactor.args().getText()));
            }

            @Override
            public void exitExpr(LyraParser.ExprContext ctx) {
                if (ctx.getText().indexOf("4640") < 0) return;
                if (ctx.unaryexpr() == null) return;
                if (ctx.unaryexpr().factor() == null) return;
                if (!(ctx.unaryexpr().factor() instanceof LyraParser.MemberFactorContext)) return;

                visited[1] = true;
                LyraParser.MemberFactorContext set
                        = (LyraParser.MemberFactorContext) ctx.unaryexpr().factor();

                assertEquals("__set", set.IDENT().getText());
                assertTrue(Pattern.matches("\\(*1\\)*", set.args().expr(0).getText()));
                assertTrue(Pattern.matches("\\(*4640\\)*", set.args().expr(1).getText()));

                assertTrue(set.factor() instanceof LyraParser.MemberFactorContext);
                LyraParser.MemberFactorContext at = (LyraParser.MemberFactorContext) set.factor();
                assertEquals("__at", at.IDENT().getText());
                assertTrue(Pattern.matches("\\(*0\\)*", at.args().expr(0).getText()));
                assertTrue(Pattern.matches("\\(*arr\\)*", at.factor().getText()));
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
        assertTrue(visited[1]);
    }

    @Test
    public void testRewriteArrayConstructor() throws Exception {
        Compiler compiler = new Compiler();
        compiler.init(getReader("samples/Rewrite2DArrayAccess.ly"));
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());
        walker.walk(new ArrayRewriterListener(), compiler.getParseTree());

        final boolean visited[] = {false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitObjectAlocExpr(LyraParser.ObjectAlocExprContext ctx) {
                visited[0] = true;

                assertEquals("Int$Array$Array", ctx.IDENT().getText());
                assertEquals(2, ctx.args().expr().size());
                assertTrue(Pattern.matches("\\(*2\\)*", ctx.args().expr(0).getText()));
                assertTrue(Pattern.matches("\\(*3\\)*", ctx.args().expr(1).getText()));
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
    }
}
