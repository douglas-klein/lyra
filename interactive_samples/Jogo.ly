class Application {

  def main {
    out.writeln("Bem vindo!");
    out.writeln("Qual o seu nome?");
    String nome = in.readln();

    Int numeroEscolhido = 14;
    out.writeln(nome + ", tente adivinhar um número entre 0 e 20");
    while true {
      Int numero = in.readln().parseInt();
      if numero == numeroEscolhido {
        out.writeln("Parabéns, você acertou! Deseja tentar de novo?");
        Bool deNovo = in.readln().parseBool();
        if deNovo {
           numeroEscolhido = novoNumero(numeroEscolhido);
           out.writeln(nome + ", tente adivinhar um número entre 0 e 20");
        } else {
          break;
        }
      } else {
        out.writeln("Número errado! Tente novamente.");
      }
    }
    out.writeln("Adeus amigo.");
  }

  def novoNumero(antigo : Int) : Int {
    Int novo = antigo + 9;
    if novo > 20 {
      novo = novo - 20;
    }
	return novo;    
  }
}
