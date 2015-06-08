## Análise Semântica

### Reescritas de subárvores

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

### Verificação atributo semântico type

- TODO: descrever TypeListener e mostrar resolução de overload

### Verificação de outras regras semânticas