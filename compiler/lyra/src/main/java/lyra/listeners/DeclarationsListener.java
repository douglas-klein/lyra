package lyra.listeners;

import lyra.Compiler;
import lyra.LyraParser;
import lyra.scopes.BaseScope;
import lyra.symbols.*;
import lyra.scopes.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Created by eduardo on 29/04/15.
 */
public class DeclarationsListener extends lyra.LyraParserBaseListener {
    private ParseTreeProperty<Scope> scopes;
    private BaseScope globals;
    private Scope currentScope; // define symbols in this scope

    public DeclarationsListener(ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
    }

    @Override
    public void enterProgram(lyra.LyraParser.ProgramContext ctx) {
        globals = new BaseScope(null);
        currentScope = globals;
    }

    @Override
    public void exitProgram(lyra.LyraParser.ProgramContext ctx) {
        leaveScope();
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
        leaveScope();
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
        leaveScope();
    }

    @Override
    public void enterScopestat(LyraParser.ScopestatContext ctx) {
        enterAnonymousScope(ctx);
    }

    @Override
    public void exitScopestat(LyraParser.ScopestatContext ctx) {
        leaveScope();
    }

    @Override
    public void enterForstat(LyraParser.ForstatContext ctx) {
        enterAnonymousScope(ctx);
    }

    @Override
    public void enterWhilestat(LyraParser.WhilestatContext ctx) {
        enterAnonymousScope(ctx);
    }

    @Override
    public void exitWhilestat(LyraParser.WhilestatContext ctx) {
        leaveScope();
    }

    @Override
    public void enterForever(LyraParser.ForeverContext ctx) {
        enterAnonymousScope(ctx);
    }

    @Override
    public void exitForever(LyraParser.ForeverContext ctx) {
        leaveScope();
    }

    @Override
    public void enterSwitchstat(LyraParser.SwitchstatContext ctx) {
        enterAnonymousScope(ctx);
    }

    @Override
    public void exitSwitchstat(LyraParser.SwitchstatContext ctx) {
        leaveScope();
    }

    @Override
    public void enterIfstat(LyraParser.IfstatContext ctx) {
        enterAnonymousScope(ctx);
    }

    @Override
    public void exitIfstat(LyraParser.IfstatContext ctx) {
        leaveScope();
    }

    @Override
    public void enterElsestat(LyraParser.ElsestatContext ctx) {
        enterAnonymousScope(ctx);
    }

    @Override
    public void exitElsestat(LyraParser.ElsestatContext ctx) {
        leaveScope();
    }

    private void leaveScope() {
        currentScope = currentScope.getEnclosingScope();
    }
    private void enterAnonymousScope(ParserRuleContext ctx) {
        BaseScope scope = new BaseScope(currentScope);
        saveScope(ctx, scope);
        currentScope = scope;
    }

    public BaseScope getGlobals() {
        return globals;
    }

    void saveScope(ParserRuleContext ctx, Scope s) { scopes.put(ctx, s); }
}
