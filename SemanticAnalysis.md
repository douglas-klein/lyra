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
- TODO Descrever como reescrever
```java 
    /* colocar código aqui */
```

#### Verificação atributo semântico type
> TODO: descrever TypeListener e mostrar resolução de overload

#### LocalVarUsageListener
> TODO mostrar pedaço do código

#### TypeListener
> TODO mostrar epdaço do código

##### Resolução de Overloads
> TODO mostrar pedaço do código

#### AssertListener
> TODO mostrar pedaço do código
