import "math";
/*  Comentário de bloco */
// Comentário de linha

class dog extends animal{

	def constructor (int patas, string nome){
		super(patas, nome);
	}
	
	def falar {
		out.print("AU AU");
	}

	def comer {
		out.print("O cachorro est comendo...");
	}

}
class Application {

	def main {

		animal buster = new dog("Buster");
		animal lola   = new dog ("Lola", 2);

		out.print(rex.falar);

		lola.comer;
		lola.dormir(true);

		int a = new int(23);  // faz casting para int

		string bb = new string(a); // faz casting para string

		int i = 1;
		string array[] = new Array("A", "B", "C");

	}
}