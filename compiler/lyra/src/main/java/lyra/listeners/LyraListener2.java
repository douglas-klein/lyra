package lyra.listeners;

import lyra.LyraParser;
import lyra.scopes.BaseScope;
import lyra.scopes.Scope;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.Compiler;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Created by eduardo on 29/04/15.
 */
public class LyraListener2 extends lyra.LyraParserBaseListener {
    private ParseTreeProperty<Scope> scopes = new ParseTreeProperty<>();
    private BaseScope globals;
    private Scope currentScope; // define symbols in this scope
    private Compiler compiler;

    public LyraListener2(BaseScope globals, ParseTreeProperty<Scope> scopes, Compiler compiler) {
        this.scopes = scopes;
        this.globals = globals;
        this.compiler = compiler;
    }

    @Override
    public void enterProgram(LyraParser.ProgramContext ctx) {
        currentScope = globals;
    }

    @Override
    public void enterClassdecl(LyraParser.ClassdeclContext ctx) {
        currentScope = scopes.get(ctx);
    }

    @Override
    public void exitAttribute_decl(LyraParser.Attribute_declContext ctx) {
        String name = ctx.IDENT().get(0).getText();
        Symbol var = currentScope.resolve(name);
        if ( var==null ) {
         // this.compiler.getErrorListener().syntaxError(compiler.getParser(),ctx.IDENT().get(0),0,0,"SADBOYS",null);
        }
        if ( var instanceof MethodSymbol) {
          //  Compiler.error(ctx.IDENT().get(0).getSymbol(), name + " is not a variable");
        }
    }

    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }

    public BaseScope getGlobals() {
        return globals;
    }
}
