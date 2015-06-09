package lyra.listeners;

import java.util.ArrayList;
import java.util.List;

import lyra.Compiler;
import lyra.LyraParser.AttributeDeclContext;
import lyra.LyraParser.ClassdeclContext;
import lyra.LyraParser.MethodDeclContext;
import lyra.symbols.ClassSymbol;
import lyra.symbols.InterfaceSymbol;
import lyra.symbols.MethodSymbol;
import lyra.symbols.Symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AbstractMethodListener extends ScopedBaseListener {
	
    protected AbstractMethodListener(Compiler compiler) {
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
    	// TODO exitClassdecl
    	// verificar se classe implementa todos os métodos das interfaces e outras classes abstratas (caso ele mesmo não seja abstrato)
    	
		ClassSymbol classSymbol = (ClassSymbol) table.getNodeSymbol(ctx);
		boolean hasAbstractMethods = classSymbol.getOverloads().anyMatch(m -> m.isAbstract());
		
		if(!classSymbol.isAbstract() && hasAbstractMethods){
			
		}
    	

    	List<String> declaredMethodNames = new ArrayList<>();
    	

    	if(ctx.extendsdecl() != null){
    		TerminalNode superClassNode = ctx.extendsdecl().IDENT();
    		Symbol sSymbol = currentScope.resolve(superClassNode.getText());
    		ClassSymbol superClassSymbol = (ClassSymbol) sSymbol;
    		if(superClassSymbol.isAbstract()){
    			// Obter todos os métodos abstratos (se classe é abstrata)
    			
    		} else if (superClassSymbol.isFinal()){
    	        compiler.getErrorListener()
        		.semanticError(compiler.getParser(), 
        		classSymbol,
                "Cannot subclass a final class: "+ superClassSymbol.getQualifiedName() +".");
    		}
    	}
    	
    	if(ctx.implementsdecl() != null){
    		for(TerminalNode interfaceNode : ctx.implementsdecl().identList().IDENT()){
    			Symbol iSymbol = currentScope.resolve(interfaceNode.getText());
    			if(iSymbol == null | !(iSymbol instanceof InterfaceSymbol)){
    				expectedTypeError(interfaceNode);
    			}
    			InterfaceSymbol interfaceSymbol = (InterfaceSymbol) iSymbol;
    			// Obter todos os métodos definidos na interface
    		}
    	}
    	
    	
    	// Verifica se todos os métodos declarados explicitamente nesta classe foram reolvidos
    	for(MethodDeclContext method: ctx.classBody().methodDecl()){
    		Symbol mSymbol = currentScope.resolve(method.IDENT().getText());
    		if(mSymbol == null | !(mSymbol instanceof MethodSymbol)){
    			expectedTypeError(method);
    		}
    		MethodSymbol methodSymbol = (MethodSymbol) mSymbol;
    		//method.
    	}
    	
    	// Verifica se todos os atributos declarados explicitamente nesta classe foram reolvidos
    	for(AttributeDeclContext attribute: ctx.classBody().attributeDecl()){
    		Symbol attributeSymbol = currentScope.resolve(attribute.varDecl().type().IDENT().getText());
    		if(attributeSymbol == null | !(attributeSymbol instanceof MethodSymbol)){
    			expectedTypeError(attribute);
    		}
    	}
    	
    	
    	
    	
    		
    }
	


}
