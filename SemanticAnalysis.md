## Análise Semântica

### Passos
1. Processamento de imports
2. Reescrita de sub-árvores
3. Tabela de símbolos
  - Declarações (`DeclarationsListener`)
  - Resolução de Referências cruzadas (`ReferencesListener`)
4. Análise de regras semânticas
  - Uso antes da definição de variáveis locais (`LocalVarUsageListener`)
  - Síntese de atributos `type` (`TypeListener`)
    - Resolução de tipos e decoração da árvore
    - Solicita resolução de overloads de métodos
    - Algumas verificações oportunas de tipos (convertible, isA)
      - `X.isA(Y)` sse Y é uma classe ou interface ancestral de X
      - `X.convertible(Y)` sse `X.isA(Y)` ou `Y` possui um construtor cujo único 
         argumento é um `T` tal que `X.isA(T)`.
  - Outras regras (`assert_*`) da especificação semântica (`AssertListener`)


#### Imports
- `importdecl -> import STRING ';'` 
  - `STRING` deve ser o caminho de um arquivo, relativo ao arquivo sendo compilado
- Cada import cria um novo `Compiler` e realiza apenas o parsing do arquivo importado
- Todos os filhos de `program` são importados como filhos de `importdecl`

#### Reescritas de subárvores

Várias sub-árvores são reescritas usando outro não terminal da gramática:
- `while` reescrito como `for`
- `forever` reescrito como `for`
- `thisMethodFactor -> IDENT '(' args ')'` reescrito como `memberFactor -> factor '.' IDENT ( '(' args ')')?`
- Tipo de retorno de método omitido é substituído por `: void`
- Operadores reescritos como chamadas de métodos:
  - Binários: * / % + - < <= == != >= > is and or
  - Prefixados: - + !
  - Pósfixados: ++ --
- `enums` reescritos como classes
```scala
enum RomanNumerals {
    Zero, I, II, III
}
```
```scala
class RomanNumerals {
    Object __value = null;
    
    public def constructor(value : Int) : void {
        this.__value = value;
    }
    public def infix __equals(rhs : Object) : Bool {
        return this.__value.__equals(rhs);
    }
}
```
##### Como reescrever
- Crie objetos `LyraParser.*Context` fornecendo:
  - Nodo pai do nodo falso
  - `-1` como `invokingState`
- Se usar um listener, use os métodos `exit*`
```java 
public void exitWhilestat(LyraParser.WhilestatContext ctx) {
    ParserRuleContext parent = (ParserRuleContext)ctx.parent;
    LyraParser.ForstatContext replacement = new LyraParser.ForstatContext(parent, -1);
    //'for' varDecl?  ';'  expr ';' expr?  '{' statlist '}';

    replacement.addChild(new CommonToken(LyraLexer.FOR, "for"));
    replacement.addChild(new CommonToken(LyraLexer.SEMICOLON, ";"));

    LyraParser.ExprContext expr = ctx.expr();
    expr.parent = replacement;
    replacement.addChild(expr);

    replacement.addChild(new CommonToken(LyraLexer.SEMICOLON, ";"));
    replacement.addChild(new CommonToken(LyraLexer.LEFTCURLYBRACE, "{"));

    LyraParser.StatlistContext statlist = ctx.statlist();
    statlist.parent = replacement;
    replacement.addChild(statlist);

    replacement.addChild(new CommonToken(LyraLexer.RIGHTCURLYBRACE, "}"));

    replaceChild(ctx, parent, replacement);
}
protected static void replaceChild(ParseTree victim, ParserRuleContext parent,
                                   ParseTree replacement) {
    int line = getNodeLine(victim);
    if (line > 0)
        updateNodeTokensLine(replacement, line);

    final ListIterator<ParseTree> iterator = parent.children.listIterator();
    while (iterator.hasNext()) {
        if (iterator.next() == victim)
            iterator.set(replacement);
    }
}
```
- ANTLR4 não tem ferramentas para reescrita de árvores, criamos `replaceChild()` 
  e outros métodos tentam compensar isso

#### LocalVarUsageListener
- Uma passada completa no programa
- Estratégia: 
  - Ao entrar em um método, crie um escopo para esse método, com uma tabela de símbolos não associada a outras
  - Ao encontrar uma referência a uma variável:
    - Reporte um erro se:
      - A variável é local a esse método **E** ainda não foi vista por este listener, **OU**;
      - Estamos dentro do statement onde o nome é declarado. Ex: `Int v1 = 0, v2 = v1;`
  - Ao sair de um método, destrua o escopo atual

#### TypeListener
- Sintetiza o atributo `type` dos nós da árvore.
- Aponta erro semântico caso não seja possível determinar o atributo `type` de algum nó.
- Atributo computado em métodos `exit*` do listener.
- Casos mais simples:
```java
public void exitBoolFactor(LyraParser.BoolFactorContext ctx) {
    table.setNodeType(ctx, (TypeSymbol) currentScope.resolve("Bool"));
}
public void exitUnaryexpr(LyraParser.UnaryexprContext ctx) {
    table.setNodeType(ctx, table.getNodeType(ctx.factor()));
    table.setExprIsClassInstance(ctx, table.getExprIsClassInstance(ctx.factor()));
}
```
- Algumas verificações semânticas envolvendo `getNodeType()` são realizadas durante a construção:
  - `throw` é usado pois a construção da árvore nem sempre pode continuar.
```java
public void exitMemberFactor(LyraParser.MemberFactorContext ctx) {
    TypeSymbol factorType = table.getNodeType(ctx.factor());
    List<TypeSymbol> types = getArgTypes(ctx.args());

    if (types.isEmpty()) {
        /* try field access before method call */
        VariableSymbol field = factorType.resolveField(ctx.IDENT().getText());
        if (field != null) {
            if (!field.isClassField() && table.getExprIsClassInstance(ctx.factor()))
                throw expectedInstanceValue(ctx.factor());
            table.setNodeType(ctx, field.getType());
            return;
        }
    }

    /* method call */
    MethodSymbol method = factorType.resolveOverload(ctx.IDENT().getText(), types);
    if (method == null) 
        throw overloadNotFoundException(ctx.IDENT(), types);
    table.setNodeType(ctx, method.getReturnType());
}
```

##### Resolução de Overloads
- Algoritmo em `OverloadResolver.resolve(overloads, argTypes, allowConvertible)`
- Lista preliminar vem de `stream = ClassSymbol.getOverloads(name)`
  - Para todo `MethodSymbol m` em `stream`, não existe `n` em `stream` *tal que* `n` possui o mesmo nome **e** mesmo conjunto de argumentos **e**  `classe(n) isA classe(m)`.
- Simplificação de `OverloadResolver.resolve`:
```scala
resolveImpl(overloads, argIdx, argTypes, allowConvertible) {
  if (argIdx == argTypes.length) {
    //leaves only the overloads with the most specialized argument types
    overloads = fixAmbiguity(overloads);
    return overloads.length == 1 ? overloads[0] : null; 
  }
  m = resolveImpl(
    {m in overloads | argTypes[argIdx].isA(m.argTypes[argIdx])},
    argIdx + 1, argTypes, allowConvertible
  );
  if (!m && allowConvertible) {
    m = resolveImpl(
      {m in overloads | argTypes[argIdx].convertible(m.argTypes[argIdx])},
      argIdx + 1, argTypes, allowConvertible
    );
  }
}
```

#### AssertListener
- Checa todos os atributos semânticos `assert_*` da especificação semântica
```java
public void exitReturnstat(LyraParser.ReturnstatContext ctx) {
    ParserRuleContext parent = ctx.getParent();
    while (parent != null && !(parent instanceof LyraParser.MethodDeclContext)) 
        parent = parent.getParent();
    MethodSymbol method = (MethodSymbol) table.getNodeSymbol(parent);

    if (method.getReturnType().isA(table.getPredefinedClass("void"))) {
        if (ctx.expr() != null)
            reportSemanticException(returnWithExpressionInVoidMethod(ctx));
    } else {
        if (ctx.expr() == null) {
            reportSemanticException(returnWithoutExpression(ctx));
        } else {
            checkNodeIsConvertibleTo(ctx.expr(), method.getReturnType());
        }
    }
}
```

- Verifica se uma classe não abstrata implementou todos os métodos abstratos de 
  todas suas interfaces diretas ou indiretas.
```java
public void exitClassdecl(ClassdeclContext ctx) {
	ClassSymbol classSymbol = (ClassSymbol) table.getNodeSymbol(ctx);
	List<MethodSymbol> abstractMethods = classSymbol.getMethods()
              .filter(m -> m.isAbstract())
              .collect(Collectors.toList());
              
	if(!classSymbol.isAbstract() && !abstractMethods.isEmpty() )
		reportSemanticException(abstractMethodException(ctx.IDENT(), abstractMethods));
}
```
