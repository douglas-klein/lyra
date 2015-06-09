package lyra.listeners;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lyra.Compiler;
import lyra.LyraParser.ClassdeclContext;
import lyra.symbols.ClassSymbol;
import lyra.symbols.MethodSymbol;

import org.antlr.v4.runtime.ParserRuleContext;

public class AbstractMethodListener extends ScopedBaseListener {
	
    public AbstractMethodListener(Compiler compiler) {
		super(compiler);
	}
    
    @Override
    protected void beginScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = table.getNodeScope(ctx);
    }

    @Override
    protected void endScopeVisit(boolean named, ParserRuleContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

	@Override
    public void exitClassdecl(ClassdeclContext ctx) {
		ClassSymbol classSymbol = (ClassSymbol) table.getNodeSymbol(ctx);
		List<MethodSymbol> abstractMethods = classSymbol.getMethods()
                .filter(m -> m.isAbstract()).collect(Collectors.toList());
		if(!classSymbol.isAbstract() && !abstractMethods.isEmpty() ){
			reportSemanticException(abstractMethodException(ctx.IDENT(), abstractMethods));
		}
    }
	


}
