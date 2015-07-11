class Application {

  def main {

    out.writeln("Bem vindo!");
    out.writeln("Qual o seu nome?");
    String nome = in.readln();

    Int numeroEscolhido = sortearNumero();
    out.writeln(nome + ", tente adivinhar um número entre 0 e 20");
    forever {
      Int numero = in.readln().parseInt();
      if numero == numeroEscolhido {
        out.writeln("Parabéns, você acertou! Deseja tentar de novo?");
        Bool deNovo = in.readln().parseBool();
        if deNovo {
           numeroEscolhido = sortearNumero();
           out.writeln(nome + ", tente adivinhar um número entre 0 e 20");
        } else {
          break;
        }
      } else {
        out.writeln("Número errado! Tente novamente.");
      }
    }
  }

  def sortearNumero : Int {
    Random random = new Random();
	return random.nextInt(20);    
  }

}
