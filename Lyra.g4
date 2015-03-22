// Define a grammar called Hello
grammar Lyra;

// Descrição (incompleta) da linguagem, utilizando a notação do ANTLR4 (até onde consegui entender)

/* O nome do Não Terminal import foi substituido por importdecl,
 por import ser uma palavra reservada
 do java n pode ser utlizado como Não Terminal
*/
program                 : importdecl* ( classdecl | interfacedecl | enumdecl )+ ;
importdecl              : 'import' STRING ';' ;
/*  O Não Terminal class possibilitava herança multipla
    como não vamos ter isso na linguagem e sim interfaces multiplas, isso foi corrigido
*/
classdecl               : class_modifiers 'class' IDENT ('extends' IDENT)? implementsdecl '{' class_body '}' ;
class_modifiers         : visbility_modifier? ( 'final' | 'abstract' )? ;
visbility_modifier      : 'public' | 'protected' | 'private' ;
//extends               : 'extends' ident_list ; // removido
implementsdecl          : 'implements' ident_list;

class_body              : ( attribute_decl | method_decl )* ;
// Declarações podem ser do tipo Int a,b,c; ... ou ...  Int a = 1; ... ou ... Int a,b,c = 1,2,3;
// OBS : A forma ... Int a,b,c = 1,2,3; ainda não funciona na nosas gramática
attribute_decl          : type IDENT('[' ']')* (',' IDENT ('[' ']')* )* ';'
                        | type IDENT('[' ']')* (',' IDENT ('[' ']')* )* '=' ( expr | aloc_expr )';' ;

// Expressoes da forma ...1+1 ... 2*2 ... a+2 ... b*(c+a) ... b>(6/3) .. etc
expr                    : numexp (( '>' | '<' | '>=' | '<=' | '==' | '!=' | 'or' | 'and' | 'is') numexp)?;
numexp                  : term ( ('+' | '-' ) term )* ;
term                    : unaryexp ( ('*' | '/' | '%' ) unaryexp )*;
unaryexp                : ('+' | '-')? factor ;
factor                      : NUMBER | STRING | NULL | lvalue | '(' expr ')';
// Exemplos: dog.function( args )  ... ou ... dog[1].function2( args )
lvalue                  : IDENT ( '[' expr ']' | '.' IDENT ('(' args ')')?)*  ;
// Expressoes de alocação de objetos e arrays da forma ... " new Dog() " ... " new Point(2,2) ... " new Int[10] "
aloc_expr               : 'new' ( IDENT '(' args ')' | IDENT ('[' expr ']')+);

method_decl             : 'def' IDENT '(' params ')' (':' type)? '{' method_body '}' ;
method_body             : 'not implemented yet';
params                  : type IDENT (',' type IDENT)* ;
args                    : ( IDENT | NULL ) ( ',' ( IDENT | NULL ))* ;

interfacedecl           : 'interface' IDENT '{' method_decl_abstract+ '}' ;
method_decl_abstract    : 'def' IDENT '(' params ')'? (':' IDENT)? ';' ;

enumdecl                : 'enum' IDENT '{' enum_body '}' ;
enum_body               : default_enum | named_enum ;
default_enum            : IDENT (',' IDENT) ;
named_enum              : IDENT '=' ( STRING | NUMBER ) (';' IDENT '=' ( STRING | NUMBER ))* ;
    
type                    : IDENT ;
ident_list              : IDENT ( ',' IDENT )* ;
IDENT                   : [a-zA-Z+_*-] [a-zA-Z_0-9]* ;
//STRING                  : '"' ~('"') '"' ; // Trocado pela definição abaixo
STRING                  : '"' ( '\\"' | . )*? '"' ;
NUMBER                  : [+-]? ([0-9] | [1-9][0-9]*)( '.' [0-9]* )? ;
INCREMENT_DECREMENT     : ('1'..'9')('0'..'9')*('++' | '--') ;
BOOLEAN_VALUE           : 'true' | 'false' ;
NULL                    : 'null' ;
COMMENT                 : '/*' .*? '*/' -> skip ; // .*? matches anything until the first */
LINECOMMENT             : '//' .*? ('\r' | '\n') -> skip ;
WS                      : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
/* Essas 3 regras geravam recursão a esquerda indireta ... então foram removidas
expr                    : ( expr ) | arithmetic_expr | boolean_expr ;
arithmetic_expr         : expr ARITH_OP expr | STRING | NUMBER | INCREMENT_DECREMENT | IDENT ;
boolean_expr            : expr BOOLEAN_OP expr | BOOLEAN_VALUE | IDENT ;

Essas 3 regras não são mais utilizadas, pois só eram chamadas por outras regras
que foram removidas devido a recursão a esquerda
operator                :  ARITH_OP | BOOLEAN_OP ;
ARITH_OP                : '+' | '-' | '*' | '/' | '%' ;
BOOLEAN_OP              : '==' | '<' | '<=' | '>' | '>=' | 'or' | 'and' | 'is' ;
*/
