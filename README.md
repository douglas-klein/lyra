# Linguagem de Programação para a disciplina de Construção de Compiladores (INE5429) do curso de Ciência da Computação da UFSC

### Características da linguagem:
- Imperativa
- Orientada a Objetos
- Fortemente Tipada
- Baseada em Java, C++ e Scala e Ruby

Hello world:

```ruby
class Application {

    def main {
        out.print('Hello World!');
    }
}
```

### Palavras reservadas:
abstract case class continue def default Double enum else extends final float for if implements import Int interface null panic private protected public return String super switch this void while

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

if
```ruby
if x >= 2 {
    umaCoisa();
} else {
    outraCoisa();
}
```

for
```ruby
for Int i = 0; i < 10; i++; {
    coisa(i);
}
```

while
```ruby
while objeto.cansado {
    objeto.descansar();
}
```

switch
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

####Classes e interfaces

##Orientção a objetos
