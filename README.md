# Linguagem de Programação Lyra

### Características da linguagem:
- Imperativa
- Orientada a Objetos
- Fortemente Tipada
- Baseada em Java, C++ e Scala e Ruby
- Permite Métodos infixados

###Hello world

```ruby
class Application {

    def main {
        out.print('Hello World!');
    }
}
```

### Palavras reservadas:
abstract and break case class continue def default Double enum else extends final Float for forever if implements import infix Int interface is new null or panic private protected public return String super switch this void while

### Estruturas sintáticas

####Atribuição
```ruby
Int nome = "Buster";
Object obj = new PostModernistObject();
```

####Declaração e chamada de método

A sintaxe da declaração de métodos é semelhante a Scala.
Métodos que não têm parâmetros dispensam o uso de parênteses.

```ruby
def quadrado(int numero) : Int {
    return numero * numero;
}

def foo : Boolean {
    return true;
}

def latir {
    out.print("AU AU");
}
```

####Estruturas de controle

- if
```ruby
if x >= 2 {
    umaCoisa();
} else {
    outraCoisa();
}
```

- for
```ruby
for Int i = 0; i < 10; i++; {
    coisa(i);
}
```

- while
```ruby
while objeto.cansado {
    objeto.descansar();
}
```

- switch
```ruby
switch documento.tipo {
    case "RG":
        validaRG(documento.numero);
        verificarOrgaoExpeditor;
        //não é necessário break case acaba aqui
    case "CPF":
        validaCPF(documento.numero);
    case default:
        panic();
}
```

- forever
```ruby
forever {
	out.print('Executando para sempre');
}
```

- Métodos infixados

```ruby
class Cachorro {
	def infix come (Comida c) : Cachorro { ... }
}

class Comida { 
	private def infix __mul(Comida c, Int n) : Comida { ... }
}
	

rex come carne * 2 	//  rex come (carne * 2)  ->  rex.come(carne * 2);
>>> rex
```


### Precedência de operadores

nr | precedência (crescente)
---|------------------------
 1 | métodos infixados
 2 | or
 3 | and
 4 | is, ==, !=
 5 | <, <=, >, >=
 6 | +, -
 7 | *, /, %
 8 | !, +, - (op. unários)


##Orientação a objetos
- Tudo é objeto
- Permite sobrescrita de operadores


####Classes e interfaces
```ruby

interface Countable {
    def count : Int;
}

interface Comparable {
    def compare (Comparable rhs) : Int;
}

class ConcreteObject implements Countable, Comparable {
    def count : Int {
        return 1;
    }

    def compare (Comparable rhs) : Int {
    	...
    }
}
```

### Diagramas de Sintaxe

- [Program] (http://htmlpreview.github.io/?https://github.com/douglasklein2/ant/blob/master/syntax_graphs/Lyra.g4.html#program)
- [Declaração de classe] (http://htmlpreview.github.io/?https://github.com/douglasklein2/ant/blob/master/syntax_graphs/Lyra.g4.html#classdecl)
- [IF] (http://htmlpreview.github.io/?https://github.com/douglasklein2/ant/blob/master/syntax_graphs/Lyra.g4.html#ifstat)
- [Statement] (http://htmlpreview.github.io/?https://github.com/douglasklein2/ant/blob/master/syntax_graphs/Lyra.g4.html#statement)
- [Declaração de Método] (http://htmlpreview.github.io/?https://github.com/douglasklein2/ant/blob/master/syntax_graphs/Lyra.g4.html#method_decl)

