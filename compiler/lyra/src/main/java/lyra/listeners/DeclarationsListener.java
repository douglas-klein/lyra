package lyra.listeners;

import lyra.Compiler;
import lyra.LyraParser;
import lyra.SemanticErrorException;
import lyra.scopes.BaseScope;
import lyra.symbols.*;
import lyra.scopes.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

public class DeclarationsListener extends ScopedBaseListener {

    public DeclarationsListener(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void enterMethodDecl(lyra.LyraParser.MethodDeclContext ctx) {
        String name = ctx.IDENT().getText();
        UnresolvedType type ;
        // Quando o tipo do método estiver explicito
        if(ctx.type() != null){
            // O tipo de retorno do método já está definido na declaração do método
            type = new UnresolvedType(ctx.type().IDENT().getText());
        }
        // Quando o tipo do método for não estiver explicito então seu tipo é void
        else{
            type = Compiler.types.get("Void");
        }
        // push new scope by making new one that points to enclosing scope
        MethodSymbol method = new MethodSymbol(name, type, currentScope);
        try {
            currentScope.define(method); // Define function in current scope
        } catch (SemanticErrorException e) {
            reportSemanticException(ctx.IDENT(), e);
        }
        table.setNodeSymbol(ctx, method);
        saveScope(ctx, method);// Push: set function's parent to current
        Scope parentScope = currentScope;
        currentScope = method; // Current scope is now function scope

        /* Add "this" to method scope */
        TypeSymbol typeSymbol;
        if ((parentScope instanceof ClassSymbol) || (parentScope instanceof InterfaceSymbol)) {
            typeSymbol = (TypeSymbol) parentScope;
        } else {
            compiler.getErrorListener().semanticError(compiler.getParser(), ctx.IDENT(),
                    "Methods may only be declared inside a class or interface");
            return;
        }
        currentScope.define(new VariableSymbol("this", typeSymbol));
    }

    @Override
    public void enterMethodDeclAbstract(LyraParser.MethodDeclAbstractContext ctx) {
        //TODO repeated code from enterMethodDecl.
        String name = ctx.IDENT().getText();
        UnresolvedType type ;
        if(ctx.type() != null) {
            // O tipo de retorno do método já está definido na declaração do método
            type = new UnresolvedType(ctx.type().IDENT().getText());
        } else {
            // Quando o tipo do método for não estiver explicito então seu tipo é void
            type = Compiler.types.get("Void");
        }
        // push new scope by making new one that points to enclosing scope
        MethodSymbol method = new MethodSymbol(name, type, currentScope);
        try {
            currentScope.define(method); // Define function in current scope
        } catch (SemanticErrorException e) {
            reportSemanticException(ctx.IDENT(), e);
        }
        table.setNodeSymbol(ctx, method);
        saveScope(ctx, method);// Push: set function's parent to current
        currentScope = method; // Current scope is now function scope
    }

    @Override
    public void exitParamDecl(LyraParser.ParamDeclContext ctx) {
        LyraParser.ParamsContext params = (LyraParser.ParamsContext) ctx.getParent();
        MethodSymbol method = (MethodSymbol) table.getNodeScope(params.getParent());
        UnresolvedType type = new UnresolvedType(ctx.type().IDENT().getText());
        String name = ctx.IDENT().getText();
        VariableSymbol arg = new VariableSymbol(name, type);
        method.addArgument(arg);
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
        table.setNodeSymbol(ctx, clas);
        saveScope(ctx, clas); // Push: set classes's parent to current
        currentScope = clas;  // Current scope is now class scope
    }

    @Override
    public void enterInterfacedecl(LyraParser.InterfacedeclContext ctx) {
        String name = ctx.IDENT().getText();
        InterfaceSymbol symbol = new InterfaceSymbol(name, currentScope);
        currentScope.define(symbol);
        table.setNodeSymbol(ctx, symbol);
        saveScope(ctx, symbol);
        currentScope = symbol;
    }

    @Override
    public void enterVarDeclUnit(LyraParser.VarDeclUnitContext ctx) {
        String name = ctx.IDENT().getText();
        LyraParser.VarDeclContext parent = (LyraParser.VarDeclContext)ctx.getParent();
        UnresolvedType type = new UnresolvedType(parent.type().IDENT().getText());
        VariableSymbol symbol = new VariableSymbol(name, type);

        if (parent.getParent() instanceof LyraParser.AttributeDeclContext) {
            LyraParser.AttributeDeclContext parentParent;
            parentParent = (LyraParser.AttributeDeclContext)parent.getParent();

            TerminalNode modifier = parentParent.VISIBILITYMODIFIER();
            Visibility visibility = Visibility.PUBLIC;
            if (modifier != null)
                visibility = Visibility.fromName(modifier.getText());
            symbol.setVisibility(visibility);
        }

        try {
            currentScope.define(symbol);
        } catch (SemanticErrorException e) {
            reportSemanticException(ctx.IDENT(), e);
        }
        table.setNodeSymbol(ctx, symbol);
    }

    @Override
    protected void beginScopeVisit(boolean named, ParserRuleContext ctx) {
        if (!named) {
            enterAnonymousScope(ctx);
        } else {
            throw new RuntimeException("This beginScopeVisit() implementation can't " +
                    "be called for named scopes");
        }
    }

    @Override
    protected void endScopeVisit(boolean named, ParserRuleContext ctx) {
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

    void saveScope(ParserRuleContext ctx, Scope s) { table.setNodeScope(ctx, s); }
}
