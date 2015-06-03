package lyra;

import lyra.LyraParser;
import lyra.listeners.SyntacticSugarListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 *
 */
public class SyntacticSugarListenerTests {

    @Test
    public void testRewriteWhile() throws IOException {
        Compiler compiler = new Compiler();

        InputStreamReader reader = getReader("samples/WhileAsFor.ly");
        assertNotNull(reader);
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        SyntacticSugarListener listener = new SyntacticSugarListener();
        walker.walk(listener, compiler.getParseTree());

        final boolean[] listenerAsserts = {false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitForstat(LyraParser.ForstatContext ctx) {
                listenerAsserts[0] = true;
                assertNull(ctx.varDecl());
                assertEquals(ctx.expr().size(), 1);
            }
        }, compiler.getParseTree());

        compiler.getParseTree().inspect(compiler.getParser());

        assertTrue(listenerAsserts[0]);
    }

    private InputStreamReader getReader(String name) {
        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream(name);
        InputStreamReader reader = new InputStreamReader(stream);
        return reader;
    }

    @Test
    public void testRewriteBinaryOperators() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/RewriteBinaryOperators.ly");
        assertNotNull(reader);
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        SyntacticSugarListener listener = new SyntacticSugarListener();
        walker.walk(listener, compiler.getParseTree());


        final boolean[] gotCalled = {false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitMemberFactor(LyraParser.MemberFactorContext ctx) {
                gotCalled[0] = true;
                assertEquals(ctx.IDENT().getText(), "__added");

                LyraParser.ExprContext arg = ctx.args().expr(0);
                assertNotNull(arg);
                assertTrue(arg.getText().indexOf("1") >= 0);

                assertTrue(ctx.factor().getText().indexOf("x") >= 0);
            }
        }, compiler.getParseTree());

        assertTrue(gotCalled[0]);
    }

    @Test
    public void testRewritePrefixNot() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/RewritePrefixNot.ly");
        assertNotNull(reader);
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        SyntacticSugarListener listener = new SyntacticSugarListener();
        walker.walk(listener, compiler.getParseTree());

        final boolean[] visited = {false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void enterVarDecl(LyraParser.VarDeclContext ctx) {
                if (!ctx.varDeclUnit(0).IDENT().getText().equals("truth"))
                    return;
                visited[0] = true;

                LyraParser.ExprContext expr = ctx.varDeclUnit(0).expr();
                assertNotNull(expr);
                assertNotNull(expr.unaryexpr());
                assertNotNull(expr.unaryexpr().factor());
                LyraParser.FactorContext factor = expr.unaryexpr().factor();

                assertTrue(factor instanceof LyraParser.MemberFactorContext);
                LyraParser.MemberFactorContext member = (LyraParser.MemberFactorContext) factor;

                assertEquals(member.factor().getText(), "false");
                assertEquals(member.IDENT().getText(), "__not");
                assertEquals(member.args().expr().size(), 0);
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
    }

    @Test
    public void testChainedPostFix() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/RewriteChainedIncrement.ly");
        assertNotNull(reader);
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        SyntacticSugarListener listener = new SyntacticSugarListener();
        walker.walk(listener, compiler.getParseTree());

        final boolean[] visited = {false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitVarDecl(LyraParser.VarDeclContext ctx) {
                if (!ctx.varDeclUnit(0).IDENT().getText().equals("y"))
                    return;
                visited[0] = true;

                assertEquals(1, ctx.varDeclUnit().size());
                assertNotNull(ctx.varDeclUnit(0).expr());
                assertNotNull(ctx.varDeclUnit(0).expr().unaryexpr());
                LyraParser.FactorContext f1 = ctx.varDeclUnit(0).expr().unaryexpr().factor();
                assertNotNull(f1);

                //x++++--
                //((x.__inc()).__inc()).__dec()

                assertTrue(f1 instanceof LyraParser.MemberFactorContext);
                LyraParser.MemberFactorContext m1 = (LyraParser.MemberFactorContext)f1;
                assertEquals("__dec", m1.IDENT().getText());

                LyraParser.FactorContext f2 = m1.factor();
                assertTrue(f2 instanceof LyraParser.MemberFactorContext);
                LyraParser.MemberFactorContext m2 = (LyraParser.MemberFactorContext)f2;
                assertEquals("__inc", m2.IDENT().getText());

                LyraParser.FactorContext f3 = m2.factor();
                assertTrue(f3 instanceof LyraParser.MemberFactorContext);
                LyraParser.MemberFactorContext m3 = (LyraParser.MemberFactorContext)f3;
                assertEquals("__inc", m3.IDENT().getText());

                assertEquals("x", m3.factor().getText());
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
    }

    @Test
    public void testRewriteArrayAccess() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/Rewrite1DArrayAccess.ly");
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        SyntacticSugarListener listener = new SyntacticSugarListener();
        walker.walk(listener, compiler.getParseTree());

        final boolean visited[] = {false, false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitVarDecl(LyraParser.VarDeclContext ctx) {
                if (!ctx.varDeclUnit(0).IDENT().getText().equals("x")) return;

                visited[0] = true;

                assertEquals(1, ctx.varDeclUnit().size());
                assertNotNull(ctx.varDeclUnit(0).expr());
                assertNotNull(ctx.varDeclUnit(0).expr().unaryexpr());
                LyraParser.FactorContext factor = ctx.varDeclUnit(0).expr().unaryexpr().factor();
                assertNotNull(factor);
                assertTrue(factor instanceof LyraParser.MemberFactorContext);

                LyraParser.MemberFactorContext member = (LyraParser.MemberFactorContext) factor;
                assertEquals("__at", member.IDENT().getText());
                assertEquals("arr", member.factor().getText());
                assertEquals(1, member.args().expr().size());
                assertTrue(Pattern.matches("\\(?0\\)?", member.args().expr(0).getText()));
            }

            @Override
            public void exitExpr(LyraParser.ExprContext ctx) {
                if (ctx.unaryexpr() == null) return;
                LyraParser.FactorContext factor = ctx.unaryexpr().factor();
                if (factor == null) return;
                if (!(factor instanceof LyraParser.MemberFactorContext)) return;

                LyraParser.MemberFactorContext member = (LyraParser.MemberFactorContext)factor;
                if (member.getText().indexOf("4640") < 0) return;

                visited[1] = true;

                assertEquals("arr", member.factor().getText());
                assertEquals("__set", member.IDENT().getText());

                LyraParser.ArgsContext args = member.args();
                assertEquals(2, args.expr().size());
                assertTrue(Pattern.matches("\\(?0\\)?", args.expr(0).getText()));
                assertTrue(Pattern.matches("\\(?4640\\)?", args.expr(1).getText()));
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);
        assertTrue(visited[1]);
    }

    @Test
    public void testRewriteForever() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/RewriteForever.ly");
        assertNotNull(reader);
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        SyntacticSugarListener listener = new SyntacticSugarListener();
        walker.walk(listener, compiler.getParseTree());

        final boolean[] visited = {false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitForstat(LyraParser.ForstatContext ctx) {
                visited[0] = true;

                assertNull(ctx.varDecl());
                assertEquals(1, ctx.expr().size());

                assertNotNull(ctx.expr(0).unaryexpr());
                LyraParser.FactorContext factor = ctx.expr(0).unaryexpr().factor();
                assertNotNull(factor);
                assertTrue(factor instanceof LyraParser.BoolFactorContext);

                LyraParser.BoolFactorContext bool = (LyraParser.BoolFactorContext) factor;
                assertEquals("true", bool.getText());

                assertEquals(2, ctx.statlist().statement().size());
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);

    }


}
