package lyra;

import lyra.listeners.ArrayGeneratorListener;
import lyra.listeners.SyntacticSugarListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for ArrayGeneratorListener
 */
public class ArrayGeneratorListenerTests {

    private InputStreamReader getReader(String name) {
        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream(name);
        InputStreamReader reader = new InputStreamReader(stream);
        return reader;
    }

    @Test
    public void testRewriteArrayAccess() throws Exception {
        Compiler compiler = new Compiler();
        InputStreamReader reader = getReader("samples/Rewrite1DArrayAccess.ly");
        compiler.init(reader);
        assertTrue(compiler.parse());

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SyntacticSugarListener(), compiler.getParseTree());
        walker.walk(new ArrayGeneratorListener(), compiler.getParseTree());

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
}
