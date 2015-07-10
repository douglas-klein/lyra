package lyra;

import lyra.LyraParser;
import lyra.listeners.SyntacticSugarListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
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
    public void testRewriteWhile() throws Exception {
        Compiler compiler = new Compiler();

        compiler.init(getFile("samples/WhileAsFor.ly"));
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

        assertTrue(listenerAsserts[0]);
    }

    private File getFile(String name) throws URISyntaxException {
        ClassLoader loader = getClass().getClassLoader();
        return new File(loader.getResource(name).toURI());
    }

    @Test
    public void testRewriteBinaryOperators() throws Exception {
        Compiler compiler = new Compiler();
        compiler.init(getFile("samples/RewriteBinaryOperators.ly"));
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
        compiler.init(getFile("samples/RewritePrefixNot.ly"));
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
        compiler.init(getFile("samples/RewriteChainedIncrement.ly"));
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
    public void testRewriteForever() throws Exception {
        Compiler compiler = new Compiler();
        compiler.init(getFile("samples/RewriteForever.ly"));
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

                assertEquals(3, ctx.statlist().statement().size());
            }
        }, compiler.getParseTree());

        assertTrue(visited[0]);

    }

    @Test
    public void testUnamedEnumAsClass() throws Exception {
        Compiler compiler = new Compiler();
        compiler.init(getFile("samples/Enums.ly"));
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());

        final boolean visited[] = {false, false, false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitClassdecl(LyraParser.ClassdeclContext ctx) {
                if (!ctx.IDENT().getText().equals("RomanNumerals"))
                    return;
                visited[0] = true;
            }

            @Override
            public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
                if (!ctx.IDENT().getText().equals("constructor"))
                    return;

                assertNotNull(ctx.params());
                assertEquals(1, ctx.params().paramDecl().size());
                if (ctx.params().paramDecl(0).type().getText().equals("Int")) {
                    visited[2] = true;
                }

                assertTrue(Pattern.matches(".*__value\\s*=\\s*value.*",
                        ctx.methodBody().getText()));
            }

            @Override
            public void exitAttributeDecl(LyraParser.AttributeDeclContext ctx) {
                if (!ctx.varDecl().varDeclUnit(0).IDENT().getText().equals("III"))
                    return;
                visited[1] = true;

                assertNotNull(ctx.STATIC());
                assertEquals("public", ctx.VISIBILITYMODIFIER().getText());
                assertEquals("Int", ctx.varDecl().type().IDENT().getText());

                LyraParser.FactorContext factor = ctx.varDecl().varDeclUnit(0).expr()
                        .unaryexpr().factor();
                assertTrue(factor instanceof LyraParser.NumberFactorContext);
                assertEquals("3", factor.getText());

            }
        }, compiler.getParseTree());

        for (int i = 0; i < visited.length; i++)
            assertTrue(String.format("i=%1$d", i), visited[i]);
    }


    @Test
    public void testStringEnumAsClass() throws Exception {
        Compiler compiler = new Compiler();
        compiler.init(getFile("samples/StringEnums.ly"));
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());

        final boolean visited[] = {false, false};
        walker.walk(new lyra.LyraParserBaseListener() {
            @Override
            public void exitClassdecl(LyraParser.ClassdeclContext ctx) {
                if (!ctx.IDENT().getText().equals("RomanNumerals"))
                    return;
                visited[0] = true;
            }

            @Override
            public void exitAttributeDecl(LyraParser.AttributeDeclContext ctx) {
                if (!ctx.varDecl().varDeclUnit(0).IDENT().getText().equals("III"))
                    return;
                visited[1] = true;

                assertNotNull(ctx.STATIC());
                assertEquals("public", ctx.VISIBILITYMODIFIER().getText());
                assertEquals("String", ctx.varDecl().type().IDENT().getText());

                LyraParser.FactorContext factor = ctx.varDecl().varDeclUnit(0).expr()
                        .unaryexpr().factor();
                assertTrue(factor instanceof LyraParser.StringFactorContext);
                assertEquals("\"III\"", factor.getText());

            }
        }, compiler.getParseTree());
        assertTrue(visited[0]);
        assertTrue(visited[1]);
    }


}
