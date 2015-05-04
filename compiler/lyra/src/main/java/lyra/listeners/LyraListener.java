package lyra.listeners;

import lyra.Compiler;
import lyra.scopes.BaseScope;
import lyra.symbols.*;
import lyra.scopes.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Created by eduardo on 29/04/15.
 */
public class LyraListener extends lyra.LyraParserBaseListener {
    private ParseTreeProperty<Scope> scopes = new ParseTreeProperty<>();
    private BaseScope globals;
    private Scope currentScope; // define symbols in this scope

    @Override
    public void enterProgram(lyra.LyraParser.ProgramContext ctx) {
        globals = new BaseScope(null);
        currentScope = globals;
    }

    @Override
    public void exitProgram(lyra.LyraParser.ProgramContext ctx) {
        currentScope = currentScope.getEnclosingScope(); // pop scope
       // System.out.println(globals);
    }

    @Override
    public void enterMethod_decl(lyra.LyraParser.Method_declContext ctx) {
        String name = ctx.IDENT().getText();
        Type type ;
        // Quando o tipo do método estiver explicito
        if(ctx.type() != null){
            // O tipo de retorno do método já está definido na declaração do método
            type = new Type(ctx.type().IDENT().getText());
        }
        // Quando o tipo do método for não estiver explicito então seu tipo é void
        else{
            type = Compiler.types.get("Void");
        }
        // push new scope by making new one that points to enclosing scope
        MethodSymbol method = new MethodSymbol(name, type, currentScope);
        currentScope.define(method); // Define function in current scope
        saveScope(ctx, method);// Push: set function's parent to current
        currentScope = method; // Current scope is now function scope
    }

    @Override
    public void exitMethod_decl(lyra.LyraParser.Method_declContext ctx) {
        currentScope = currentScope.getEnclosingScope(); // pop scope
    }

    @Override
    public void enterClassdecl(lyra.LyraParser.ClassdeclContext ctx) {
        String className = ctx.IDENT().getText();
        Symbol superClass = null;
        ClassSymbol clas;
        if( ctx.extendsdecl() != null) {
            String superClassName = ctx.extendsdecl().IDENT().getText();
            if( currentScope.resolve(superClassName) instanceof ClassSymbol)
                superClass = currentScope.resolve(superClassName);
        }

        // push new scope by making new one that points to enclosing scope
        clas =  new ClassSymbol(className, currentScope, (ClassSymbol)superClass);
        currentScope.define(clas); // Define class in current scope
        saveScope(ctx, clas); // Push: set classes's parent to current
        currentScope = clas;  // Current scope is now class scope
    }

    @Override
    public void exitClassdecl(lyra.LyraParser.ClassdeclContext ctx) {
        currentScope = currentScope.getEnclosingScope(); // pop scope
    }

    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }

    public BaseScope getGlobals() {
        return globals;
    }

    void saveScope(ParserRuleContext ctx, Scope s) { scopes.put(ctx, s); }
}
