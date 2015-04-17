/**
 * 
 */
package lyra;

import lyra.LyraLexer;
import lyra.LyraParser;
import org.antlr.runtime.tree.DOTTreeGenerator;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Frontend {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ANTLRInputStream in = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(args[0]));
			in = new ANTLRInputStream(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		LyraLexer lexer = new LyraLexer(in);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		LyraParser parser = new LyraParser(tokens);
		ParseTree tree = parser.program();
		System.out.println();
	}

}
