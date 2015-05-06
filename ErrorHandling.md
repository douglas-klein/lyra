## Tratamento de Erros

### ErrorListener
- `ErrorListener` (extende `BaseErrorListener`, implementa `ANTLRErrorListener`)
  - Pilha do parser `--verbose-errors`  
    ```
    Error: line 3:26 at ";": Unclosed '('  
    Parser rule stack: [program, classdecl, class_body, method_decl, method_body, 
    statlist, statement, attribute_decl, exprlist, expr, expr, unaryexpr, 
    unaryexpr_2, factor]
    ```
  
  - Inspeção de subárvores com erros: `--gui-error-context`  
    ```ruby
    class Application {  
      Int width  
      Int height;  
    }
    ```  
    ![Sub-árvore representando o erro](/ErrorHandling_imgs/missing_semicolon.png)
    
### Recuperação de Erros
- *Error Alternative*

  ```
  param_decl : IDENT COLON type
             | type IDENT {notifyErrorListeners("Lyra expects parameters declared in the form \"name : type\"");}  
  /* ... */  
  factor : /* ... */  
         | LEFTPARENTHESES expr {notifyErrorListeners("Unclosed '('");}  
  ```
  - Erros são reconhecidos (mas ainda são erros):  
  ![Sub-árvore com ')' extra](/ErrorHandling_imgs/extra_parenthesis.png)

#### Estratégias de recuperação
- Recuperação padrão ANTLR (`DefaultErrorStrategy`):
  1. Remover o token inesperado pode resolver?
  2. Inserir o token esperado pode resolver?
  3. Parser desiste da regra, consome tokens até que encontre um token no conjunto Follow. 
- Recuperação alternativa: `--lemonade-recovery`
  - Tećnica 5
    1. Símbolo previsto é removido da pilha
    2. Parsing continua a partir do símbolo da entrada que causou o erro.
  - Subclasse de `DefaultErrorStrategy`.
  - Instalado no parser gerado: `parser.setErrorHandler(new LemonadeErrorHandler());`
  - Exemplo:  

    ```ruby
    class Application {
        Int width
        Int height;
    }
    ```
    
    - DefaultErrorStrategy:  
      ![Sub-árvore de erro recuperado com DefaultErrorStrategy](/ErrorHandling_imgs/default_tree.png)
    - LemonadeErrorHandler:  
      ![Sub-árvore de erro recuperado com LemonadeErrorHandler](/ErrorHandling_imgs/lemonade_tree.png)



## Tabela de Símbolos
Nossa tabela de símbolos é uma árvore de Escopos, onde cada escopo gerencia o mapeamento de nomes para os símbolos.

Um escopo contém três métodos: 
- `define`: indexa símbolos pelo nome
- `resolve`: retorna um objeto a partir do nome, e caso não o encontre, procura recursivamente no escopo pai
- `getEnclosingScope`: retorna o escopo pai

A classe Symbol é a raiz da hierarquia de símbolos, que contém apenas o nome e o tipo do símbolo.

As especializações de símbolos são:
- classe, que contém seus métodos e atributos, além das interfaces e da super classe
- método, que contém uma lista de argumentos e o tipo de retorno
- variável

Os tokens "{ }" definem um escopo. Alguns símbolos também são escopos, como classe e método.


