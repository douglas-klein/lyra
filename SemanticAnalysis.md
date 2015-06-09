## Análise Semântica

### Passos
- Parsing (quase sem ação semântica)
- Reescrita da árvore
- Tabela de símbolos
  - Declarações (`DeclarationsListener`)
  - Resolução de Referências cruzadas (`ReferencesListener`)
- Análise de regras semânticas
  - Uso de variáveis locais antes da definição (`LocalVarUsageListener`)
  - Construção da árvore de atributos `type` (`TypeListener`)
    - Resolução de tipos e decoração da árvore
    - Solicita resolução de overloads de métodos
    - Algumas verificações oportunas de tipos (convertible, isA)
      - `X.isA(Y)` sse Y é uma classe ou interface ancestral de X
      - `X.convertible(Y)` sse `X.isA(Y)` ou `Y` possui um construtor cujo único 
         argumento é um `T` tal que `X.isA(T)`.
  - Atributos `assert_*` da especificação semântica (`AssertListener`)
  - Todos os métodos abstratos foram implementados? (`AbstractMethodListener`)
  

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
    public def constructor(value : String) : void {
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

    replaceChild(ctx, parent, replacement); //sets line on fake tokens and other stuff
}
```
- ANTLR4 não tem ferramentas para reescrita de árvores, criamos `replaceChild()` 
  e outros métodos tentam compensar isso

#### Verificação atributo semântico type
> TODO: descrever TypeListener e mostrar resolução de overload

#### LocalVarUsageListener
- Uma passada completa no programa
- Estratégia: 
  - Ao entrar em um método, crie um escopo raiz (não associado com a tabela de símbolos)
  - Ao sair de um método, fique sem esse escopo, o que desativa verificações
  - Ao encontrar uma referência à algum nome `nameFactor`:
    - Encontre o método pai do nodo atual
    - Reporte um erro se:
      - O nome é local a esse método (consulta feita na tabela de símbolos), **E**;
      - O nome não foi declarado na árvore de escopos privada desse listener, **OU**;
      - Estamos dentro do statement onde o nome é declarado.

#### TypeListener
> TODO mostrar epdaço do código

##### Resolução de Overloads
> TODO mostrar pedaço do código

#### AssertListener
> TODO mostrar pedaço do código
