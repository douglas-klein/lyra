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
        print('Hello World!');
    }
}
```

### Palavras reservadas:
abstract break case class const continue def default double enum else extends final float for if implements import in int interface null panic print private protected public read return string super switch this void while

### Estruturas sintáticas

####Atribuição
```ruby
int nome = "Buster";
```	

####Declaração e chamada de método

A sintaxe da declaração de métodos é semelhante a Scala.
Métodos que não têm parâmetros dispensam o uso de parênteses.

```ruby
def quadrado(int numero) : int {
	return numero * numero;
}

def foo? : boolean {
	return true;
}

quadrado(3);
>>> 9

foo?;
>>> true

int cubo = lambda x : x * x * x;

cubo(4)
>>> 64
```