package lyra.listeners;

import java.lang.Object;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lyra.Compiler;
import lyra.LyraParser;
import lyra.SemanticErrorException;
import lyra.scopes.Scope;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;
import lyra.symbols.SymbolTable;
import lyra.symbols.TypeSymbol;

import lyra.symbols.predefined.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Helper base class that, for all scope-creating rules, calls abstract methods
 * when entering and exiting those rules.
 *
 * This allows for a single pair of beginScopeVisit() endScopeVisit() instead of
 * overriding methods for all rules.
 */
public abstract class ScopedBaseListener extends lyra.LyraParserBaseListener {

    protected Compiler compiler;
    protected Scope currentScope; // define symbols in this scope
    protected SymbolTable table;
    private ParserRuleContext mutedSubtree;

    protected ScopedBaseListener(lyra.Compiler compiler){
        this.compiler = compiler;
        this.table = compiler.getSymbolTable();
    }

    @Override
    public void enterProgram(LyraParser.ProgramContext ctx) {
        currentScope = table.getGlobal();
    }

    @Override
    public void exitProgram(LyraParser.ProgramContext ctx) {
        currentScope = null;
    }

    protected void reportSemanticException(TerminalNode node, SemanticErrorException e) {
        e.setOffendingSymbol(node);
        reportSemanticException(e);
    }
    protected void reportSemanticException(SemanticErrorException e) {
        compiler.getErrorListener().semanticError(compiler.getParser(), e);
    }

    protected void muteSubtree(ParserRuleContext subtreeRoot) {
        if (mutedSubtree != null)
            throw new RuntimeException("Recursively muting a subtree.");
        mutedSubtree = subtreeRoot;
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if (mutedSubtree == null)
            super.enterEveryRule(ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        if (mutedSubtree == null)
            super.exitEveryRule(ctx);
        if (mutedSubtree == ctx)
            mutedSubtree = null;
    }

    @Override
    public void enterMethodDecl(LyraParser.MethodDeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitMethodDecl(LyraParser.MethodDeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterMethodDeclAbstract(LyraParser.MethodDeclAbstractContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitMethodDeclAbstract(LyraParser.MethodDeclAbstractContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterClassdecl(LyraParser.ClassdeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitClassdecl(LyraParser.ClassdeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterInterfacedecl(LyraParser.InterfacedeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitInterfacedecl(LyraParser.InterfacedeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterScopestat(LyraParser.ScopestatContext ctx) {
        beginScopeVisit(false, ctx);
    }
    @Override
    public void exitScopestat(LyraParser.ScopestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterForstat(LyraParser.ForstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitForstat(LyraParser.ForstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterWhilestat(LyraParser.WhilestatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitWhilestat(LyraParser.WhilestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterForever(LyraParser.ForeverContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitForever(LyraParser.ForeverContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterSwitchstat(LyraParser.SwitchstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitSwitchstat(LyraParser.SwitchstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterCasedecl(LyraParser.CasedeclContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitCasedecl(LyraParser.CasedeclContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterIfstat(LyraParser.IfstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitIfstat(LyraParser.IfstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterElsestat(LyraParser.ElsestatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitElsestat(LyraParser.ElsestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    protected abstract void beginScopeVisit(boolean named, ParserRuleContext ctx);
    protected abstract void endScopeVisit(boolean named, ParserRuleContext ctx);


    protected SemanticErrorException unresolvedTypeException(Object offendingSymbol,
                                                             String typeName) {
        return new SemanticErrorException("Unresolved type " + typeName + ".", offendingSymbol);
    }

    protected SemanticErrorException undefinedNameException(Object offendingSymbol) {
        return new SemanticErrorException("Undefined name.", offendingSymbol);
    }
    protected SemanticErrorException expectedVariableException(Object offendingSymbol) {
        return new SemanticErrorException("Expected a variable.", offendingSymbol);
    }

    protected SemanticErrorException noOverloadException(Object offendingSymbol, String typeName,
                                                         String methodName,
                                                         Collection<TypeSymbol> arguments) {
        String msg = String.format("No overload for %1$s.%2$s can handle args (%3$s).", typeName,
                methodName,
                arguments.stream().map(t -> t.getName())
                        .reduce("", (a, b) -> a + (a.isEmpty() ? ", " : "") + b));
        return new SemanticErrorException(msg, offendingSymbol);
    }

    protected SemanticErrorException expectedTypeException(Object offendingSymbol) {
        return new SemanticErrorException("Expected a type.", offendingSymbol);
    }

    protected SemanticErrorException expectedInterfaceException(Object offendingSymbol) {
        return new SemanticErrorException("Expected an interface.", offendingSymbol);
    }

    protected SemanticErrorException expectedClassException(Object offendingSymbol) {
        return new SemanticErrorException("Expected a class name.", offendingSymbol);
    }

    protected SemanticErrorException classInheritanceCycleException(Object offendingSymbol) {
        return new SemanticErrorException("Cycle on inheritance/interface tree.", offendingSymbol);
    }

    protected SemanticErrorException overloadNotFoundException(Object offendingSymbol,
                                                           Collection<TypeSymbol> types) {
        String typeNames = types.stream().map(TypeSymbol::getQualifiedName)
                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : ", ") + b);
        return new SemanticErrorException(
                String.format("Overload not found for arguments: (%1$s)", typeNames),
                offendingSymbol);
    }

    protected SemanticErrorException notConvertibleException(Object offendingSymbol, Symbol type,
                                                             Symbol convertible) {
        return new SemanticErrorException(
                String.format("Type %1$s is not convertible to %2$s.", convertible.getName(),
                        type.getName()),
                offendingSymbol
        );
    }

    protected SemanticErrorException expectedNamedReferenceException(Object offendingSymbol) {
        return new SemanticErrorException("Expected a named reference.", offendingSymbol);
    }

    protected SemanticErrorException expectedInstanceValue(Object offendingNode) {
        return new SemanticErrorException("Expected an an instance variable.", offendingNode);
    }

    protected SemanticErrorException varUseBeforeDefinition(Object offendingSymbol) {
        return new SemanticErrorException("Attempt to use method-local variable before it's " +
                "declaration", offendingSymbol);
    }

    protected SemanticErrorException returnWithExpressionInVoidMethod(Object offendingSymbol) {
        return new SemanticErrorException("Return with expression in void method",
                offendingSymbol);
    }

    protected SemanticErrorException returnWithoutExpression(Object offendingSymbol) {
        return new SemanticErrorException("Return without expression in non-void method",
                offendingSymbol);
    }

    protected SemanticErrorException abstractMethodException(Object offendingSymbol,
                                                             Collection<MethodSymbol> methods) {
        String msg = "Abstract methods in non-abstract class.";
        if (methods != null && !methods.isEmpty()) {
            Set<String> set = methods.stream().map(m -> m.getName()).collect(Collectors.toSet());
            msg = "Abstract methods in non-abstract class: ";
            msg += set.stream().reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        }
        return new SemanticErrorException(msg, offendingSymbol);
    }
    
    protected SemanticErrorException inheritInFinalClassException(Object offendingSymbol) {
    	return new SemanticErrorException("Attempt to inherit a final class", offendingSymbol);
	}


}
