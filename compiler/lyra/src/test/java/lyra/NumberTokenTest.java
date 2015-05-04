package lyra;

import lyra.tokens.NumberToken;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testes para lyra.tokens.NumberToken.
 */
public class NumberTokenTest {
    @Test
    public void testIntegerToken() throws Exception {
        Integer expected = 47;
        assertEquals(expected, new NumberToken(LyraLexer.NUMBER, "47").getValue());
    }

    @Test
    public void testDoubleToken() throws Exception {
        Double expected = 0.789;
        assertEquals(expected, new NumberToken(LyraLexer.NUMBER, "0.789").getValue());
    }

    @Test
    public void testShitToken() throws Exception {
        boolean caught = false;
        try {
            new NumberToken(LyraLexer.NUMBER, "47Shit");
        } catch (NumberFormatException e) {
            caught = true;
        }
        assertTrue(caught);
    }

}