# Geração de Código

### Linguagem-objeto
- Utilizamos o gerador de código Jasmin que permite gerar código em bytecode e ser executado em uma JVM. Dessa forma, nossa linguagem é independente de plataforma.
- Objetos Lyra são objetos Java
- Construtores de objetos Lyra são construtores de objetos Java (<init>)
- Os nomes de métodos Lyra são prefixados com `lyra_` para evitar conflito com métodos Java
- A classe Object do Lyra estende a classe `java.lang.Object`


### Lyra Runtime
Como não temos tipos primitivos, escrevemos as classes built-in diretamente em Java. 

As classe built-in são:
- Object
- Null
- Void
- Number
- Int
- Bool
- String
- Output
- Input
- Array

```scala
package lyra.runtime;
import java.util.ArrayList;

public class Array extends Object {
    private ArrayList<Object> data = new ArrayList<>();
    public Array(Int size) { ... }
    public Int lyra_length() { ... }
    public Object lyra___at(Int idx) { ... }
    public Object lyra___set(Int idx, Object value) { ... }
    public Object lyra_at(Int idx) { ... }
    public Object lyra_set(Int idx, Object value) { ... }
    public String lyra_toString() {
        java.lang.String fill = data.stream().map(o -> o.toString())
                .reduce((a, b) -> a + ", " + b).orElse("");
        return new String("{" + fill + "}");
    }
}
```
Métodos Java não são acessíveis para uso em programas Lyra. Entretanto, alguns métodos das classes built-in, como o `valueOf`, são usados pelo código gerado. Todos os os objetos built-in Lyra encapsulam primitivos Java.

### Como criar um programa Lyra

1. Compile o código
2. Gere um arquivo `.j` para cada classe no programa
3. Os arquivos `.j` definem classes no pacote `lyra.user`
7. Gere um arquivo `.class` a partir de cada arquivo `.j` (utilizando o Jasmin)
4. Abra o arquivo `lyra-runtime-1.0.jar` como um ZipFile (classe Java)
5. Crie o arquivo `lyra-program.jar` também como um ZipFile.
6. Copie cada entrada do `lyra-runtime-1.0.jar` para o `lyra-program.jar`
7. Copie cada `.class` para o `lyra-program.jar`
8. Feche o arquivo `lyra-program.jar` (Pronto!)

Para executar o programa, chame `java -jar lyra-program.jar`

### lyra.runtime.Start
A classe Start é responsável por iniciar as variáveis globais e o objeto Application e invocar o método `main`. Ela  é a `Main-Class` do `lyra-runtime-1.0.jar`, e, por consequência do `lyra-program.jar`


### Geração de Código (JasminListener)
Utilizamos o padrão listener para gerar código, mas estendemos o seu comportamento:
- doOnceAfter(Rule, Runnable): executa um segmento de código arbitrário após a visita completa de uma sub-árvore
- muteSubtree(Rule): Impede a visita de uma sub-árvore.
- generateLabel(Rule): Insere um label no código.


#### Gerando um IF
```scala    
public void enterIfstat(LyraParser.IfstatContext ctx) {
      super.enterIfstat(ctx);

      String endIfLabel = methodHelper.generateLabelAfter(ctx);
      String elseLabel = (ctx.elsestat() != null) ? methodHelper.generateLabel(ctx.elsestat())
                                                  : endIfLabel;

      doOnceAfter(ctx.expr(), () -> {
          /* The resulting Object from expr is stacked, but it may not be a Bool */
          checkAndDoConversion(table.getNodeType(ctx.expr()), table.getPredefinedClass("Bool"));
          /* We have a Bool on the stack top. Get it's boolean primitive and do the if */
          writer.printf("invokevirtual lyra/runtime/Bool/valueOf()Z\n" +
                  "ifeq %1$s\n", elseLabel);
          methodHelper.decStackUsage(1); /* ifne pops the boolean */
      });
      /* jump from the end of the true statlist to after the ifstat. */
      if (ctx.elsestat() != null) {
          doOnceAfter(ctx.statlist(), () -> {
              writer.printf("goto %1$s\n", endIfLabel);
          });
      }
      /* else needs no handling other than label generation */
  }

```
