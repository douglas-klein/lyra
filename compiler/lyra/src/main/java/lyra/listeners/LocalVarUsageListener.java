package lyra.listeners;

import lyra.LyraParser;
import lyra.scopes.BaseScope;
import lyra.scopes.Scope;
import lyra.symbols.Symbol;
import lyra.symbols.VariableSymbol;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Asserts that any variable at method scope is not referenced before the statement at which it
 * is declared.
 */
public class LocalVarUsageListener extends ScopedBaseListener {

    /**
     * This contains the method scope as already built by previous listeners and stored on the
     * SymbolTable associated with the method non-terminal node.
     */
    private Scope methodScope = null;

    /**
     * Scope currently being populated by this class. This only receives an actual object once we
     * enter a method. While visiting a method children, new enclosed scopes are created as needed.
     */
    private Scope myScope = null;

    /**
     * While visiting a varDecl rule, this contains a set of all names declared on this varDecl.
     * An initialier expression trying to access any of these may encounter the name on myScope
     * but the access is illegal anyway.
     */
    private HashSet<String> currentVarDeclNames = null;

    public LocalVarUsageListener(lyra.Compiler compiler) {
        super(compiler);
    }

    @Override
    protected void beginScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = table.getNodeScope(ctx);
        if (myScope != null)
            myScope = new BaseScope(myScope);
    }

    @Override
    protected void endScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = currentScope.getEnclosingScope();
        if (myScope != null)
            myScope = myScope.getEnclosingScope();
    }

    /**
     * Returns true iff name is a variable that is either undefined or defined inside the
     * current method.
     */
    private boolean isLocalVar(String name) {
        Symbol symbol = currentScope.resolve(name);
        if (symbol != null && symbol instanceof VariableSymbol) {
            return methodScope.getEnclosingScope().resolve(name) == null;
        }
        return false;
    }

    /**
     * Returns true iff we are inside a varDecl rule and a variable with the given name is
     * declared inside it.
     */
    private boolean isBeingDeclared(String name) {
        return currentVarDeclNames != null && currentVarDeclNames.contains(name);
    }

    /**
     * Returns true iff the variable with the given name was already declared in this method.
     */
    private boolean isDeclared(String name) {
        return myScope.resolve(name) != null;
    }

    @Override
    public void enterMethodDecl(LyraParser.MethodDeclContext ctx) {
        currentScope = table.getNodeScope(ctx);
        methodScope = currentScope;
        myScope = new BaseScope(null);

        /* predefined vars */
        myScope.define(new VariableSymbol("this", table.getPredefinedClass("Object")));
    }

    @Override
    public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
        currentScope = currentScope.getEnclosingScope();
        methodScope = null;
        myScope = null;
    }

    @Override
    public void enterVarDecl(LyraParser.VarDeclContext ctx) {
        currentVarDeclNames = ctx.varDeclUnit().stream().map(c -> c.IDENT().getText())
                .collect(Collectors.toCollection(HashSet<String>::new));
    }

    @Override
    public void exitVarDecl(LyraParser.VarDeclContext ctx) {
        currentVarDeclNames = null;
    }

    @Override
    public void enterParamDecl(LyraParser.ParamDeclContext ctx) {
        if (myScope == null) return;

        myScope.define(new VariableSymbol(ctx.IDENT().getText(),
                table.getPredefinedClass("Object")));
    }

    @Override
    public void enterVarDeclUnit(LyraParser.VarDeclUnitContext ctx) {
        if (myScope == null) return;

        myScope.define(new VariableSymbol(ctx.IDENT().getText(),
                table.getPredefinedClass("Object")));
    }

    @Override
    public void exitNameFactor(LyraParser.NameFactorContext ctx) {
        if (methodScope == null)
            return;

        String name = ctx.IDENT().getText();
        if (isLocalVar(name) && !isDeclared(name) || isBeingDeclared(name)) {
            /* within method scope and sub-scopes, referencing a name before it's declarationi
             * (inside a varDecl non-terminal) is illegal.
             * Also accessing a variable from the varDecl where it was declared is illegal. */
            reportSemanticException(varUseBeforeDefinition(ctx.IDENT()));
        }
    }

    @Override
    public void exitMethodBody(LyraParser.MethodBodyContext ctx) {
        methodScope = null;
    }
}
