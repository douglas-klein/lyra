package lyra.listeners;

import lyra.LyraParser;
import lyra.scopes.BaseScope;
import lyra.scopes.Scope;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.Compiler;
import lyra.symbols.VariableSymbol;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Created by eduardo on 29/04/15.
 */
public class ReferencesListener extends lyra.LyraParserBaseListener {
    private ParseTreeProperty<Scope> scopes = new ParseTreeProperty<>();
    private BaseScope globals;
    private Scope currentScope; // define symbols in this scope
    private Compiler compiler;

    public ReferencesListener(BaseScope globals, ParseTreeProperty<Scope> scopes, Compiler compiler) {
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
        if (var == null) {
//            compiler.getErrorListener().semanticError(compiler.getParser(), ctx.IDENT(0),
//                    String.format("Undefined name, compiler bug?"));
        }
        if (!(var instanceof VariableSymbol)) {
//            compiler.getErrorListener().semanticError(compiler.getParser(), ctx.IDENT(0),
//                    String.format("Symbol %1$s already defined as something other than VariableSymbol.",
//                            ctx.IDENT(0).getText()));
        }
    }

    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }

    public BaseScope getGlobals() {
        return globals;
    }
}
