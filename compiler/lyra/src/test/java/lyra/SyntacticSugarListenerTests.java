package lyra;

import lyra.LyraParser;
import lyra.listeners.SyntacticSugarListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;

import java.io.*;

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

        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream("samples/WhileAsFor.ly");
        assertNotNull(stream);
        compiler.init(new InputStreamReader(stream));
        compiler.parse();

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
}
