package lyra.listeners;

import lyra.*;
import lyra.Compiler;
import lyra.LyraParser;
import lyra.tokens.StringToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Rewrites imports with the parse tree originated from the files referenced.
 */
public class ImportRewriterListener extends TreeRewriterBaseListener {
    private Compiler compiler;

    public ImportRewriterListener(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void exitImportdecl(LyraParser.ImportdeclContext ctx) {
        String fileName = ((StringToken)ctx.STRING().getSymbol()).getContent();
        File file = this.compiler.resolveInclude(fileName);
        Compiler compiler = new Compiler();
        FileReader reader = null;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            this.compiler.getErrorListener().semanticError(this.compiler.getParser(),
                    ctx.STRING(), "File \"" + fileName + "\" not found");
            return;
        }
        try {
            compiler.init(reader, file);
        } catch (IOException e) {
            this.compiler.getErrorListener().semanticError(this.compiler.getParser(), ctx.STRING(),
                    "Error reading file \"" + fileName + "\": " + e.getMessage());
            return;
        }

        /* compile the file redirecting errors to our listener */
        compiler.setErrorListener(this.compiler.getErrorListener());
        compiler.parse();

        /* steal all children of program and make them childs of import */
        for (ParseTree subtree : compiler.getParseTree().children) {
            ParserRuleContext child = (ParserRuleContext) subtree;
            child.parent = ctx;
            ctx.addChild(child);
        }
        compiler.getParseTree().children.clear();
    }
}
