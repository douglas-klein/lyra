import "math"


/*  Comentário de bloco */

// Comentário de linha


interface SerVivo {
	def respirar;
}


enum Cor {
	AZUL = 'azul';
	VERMELHO = 'vermelho';
}

enum Cor {AZUL, VERMELHO}



abstract class animal {
	
	Cor cor = Col.AZUL;

	final int patas;
	final string nome;
	private boolean dormindo;

	 def void abstract falar;

	 def void comer {
	 	print(nome + ' comeu.');
	 }

	 def void dormir(boolean dormir) {
	 	this.dormir = dormir;
	 }

	 def boolean dormindo? {
	 	return this.dormindo;
	 }
}

class dog extends animal{

	def constructor (int patas, string nome){
		super(patas, nome);
	}
	
	def void falar {
		print('AU AU');
	}

	def void comer {
		print('O cachorro está comendo...');
		super.comer;
	}

}

class Application {

	def main {

		animal buster = new dog('Buster');
		animal lola = new dog ('Lola', 2);

		print(rex.falar);

		lola.comer;
		lola.dormir(true);

		int a = int(2.23);  // faz casting para int

		string bb = string(a); // faz casting para string

		int i = 1;
		while buster.dormindo {
			print('buster está dormindo. Tentando acordá-lo pela %dª vez');
			int r = int(math.random() * 10);
			if r > 7 {
				buster.dormir(false);
				print('Buster acordou!');
			} else {
				i++;
			}
		}


		switch tipo {
			case 1: 
				print('A'); 

			case 2: 
				print('B');

			default:
				print('default');
		}

		int soma = 0;
		for(int i = 0; i < 10000; i++) {
			soma += i;
			if soma > 100 { 
				break
			};
		}

		string[] array = ["A", "B", "C"];

		for string a in array {
			print(a);
		}

	}
}
