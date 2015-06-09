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
        UnresolvedType type = new UnresolvedType(ctx.type().IDENT().getText());
        // push new scope by making new one that points to enclosing scope
        MethodSymbol method = new MethodSymbol(name, type, currentScope);
        try {
            currentScope.define(method); // Define function in current scope
        } catch (SemanticErrorException e) {
            reportSemanticException(ctx.IDENT(), e);
        }

        method.setInfix(ctx.INFIX() != null);
        method.setAbstract(true);

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
        UnresolvedType type = new UnresolvedType(ctx.type().IDENT().getText());
        // push new scope by making new one that points to enclosing scope
        MethodSymbol method = new MethodSymbol(name, type, currentScope);
        try {
            currentScope.define(method); // Define function in current scope
        } catch (SemanticErrorException e) {
            reportSemanticException(ctx.IDENT(), e);
        }

        method.setInfix(ctx.INFIX() != null);
        method.setAbstract(true);

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

        //start extending Object, soon we will enter extendsdecl and refine this
        ClassSymbol clas = new ClassSymbol(className, currentScope,
                table.getPredefinedClass("Object"));
        clas.setFinal(ctx.FINAL() != null);
        clas.setAbstract(ctx.ABSTRACT() != null);

        currentScope.define(clas); // Define class in current scope
        table.setNodeSymbol(ctx, clas);
        saveScope(ctx, clas); // Push: set classes's parent to current
        currentScope = clas;  // Current scope is now class scope
    }

    @Override
    public void exitClassdecl(LyraParser.ClassdeclContext ctx) {
        ((ClassSymbol)currentScope).addPredefinedMembers(table.getGlobal());
        super.exitClassdecl(ctx);
    }

    @Override
    public void exitExtendsdecl(LyraParser.ExtendsdeclContext ctx) {
        if (!(ctx.getParent() instanceof LyraParser.ClassdeclContext))
            return;
        LyraParser.ClassdeclContext parent = (LyraParser.ClassdeclContext) ctx.getParent();
        ClassSymbol classSymbol = (ClassSymbol)table.getNodeSymbol(parent);

        Symbol symbol = currentScope.resolve(ctx.IDENT().getText());
        if (symbol == null || !(symbol instanceof ClassSymbol)) {
            reportSemanticException(expectedClassException(ctx.IDENT()));
            return;
        }
        ClassSymbol superClass = (ClassSymbol)symbol;
        if (superClass.isA(classSymbol)) {
            reportSemanticException(classInheritanceCycleException(ctx.IDENT()));
            return;
        }

        classSymbol.setSuperClass(superClass);
    }

    @Override
    public void exitImplementsdecl(LyraParser.ImplementsdeclContext ctx) {
        if (!(ctx.getParent() instanceof LyraParser.ClassdeclContext)) return;
        LyraParser.ClassdeclContext parent = (LyraParser.ClassdeclContext) ctx.getParent();
        ClassSymbol classSymbol = (ClassSymbol) table.getNodeSymbol(parent);

        for (TerminalNode node : ctx.identList().IDENT()) {
            Symbol symbol = currentScope.resolve(node.getText());
            if (symbol == null || !(symbol instanceof InterfaceSymbol)) {
                reportSemanticException(expectedInterfaceException(node));
                return;
            }
            InterfaceSymbol iface = (InterfaceSymbol)symbol;
            if (iface.isA(classSymbol)) {
                reportSemanticException(classInheritanceCycleException(node));
                return;
            }
            classSymbol.addInterface(iface);
        }
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
    public void enterSuperInterfaces(LyraParser.SuperInterfacesContext ctx) {
        if (!(ctx.getParent() instanceof LyraParser.InterfacedeclContext))
            return;

        LyraParser.InterfacedeclContext parent = (LyraParser.InterfacedeclContext) ctx.getParent();
        InterfaceSymbol iface = (InterfaceSymbol) table.getNodeSymbol(parent);

        for (TerminalNode node : ctx.IDENT()) {
            Symbol symbol = currentScope.resolve(node.getText());
            if (symbol == null || !(symbol instanceof InterfaceSymbol)) {
                reportSemanticException(expectedInterfaceException(node));
            }
            InterfaceSymbol superIface = (InterfaceSymbol)symbol;
            if (superIface.isA(iface)) {
                reportSemanticException(classInheritanceCycleException(node));
                return;
            }
            iface.addSuperInterfaces(superIface);
        }
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

            symbol.setClassField(parentParent.STATIC() != null);
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
