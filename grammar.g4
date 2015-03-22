// Define a grammar called Hello
grammar Hello;

// Descrição (incompleta) da linguagem, utilizando a notação do ANTLR4 (até onde consegui entender)

// Mudei o nome do Não Terminal import por importdecl, 
// por import ser uma palavra reservada
// do java n pode ser utlizado como Não Terminal
program                 : importdecl* ( class | interface | enum )+ ; 
importdecl              : 'import' STRING ';' ;
// Mudei o Não Terminal class, antes ele possibilitava herança multipla
// Pelo que lembro não vamos ter isso na linguagem e sim interfaces multiplas
class                   : class_modifiers 'class' IDENT ('extends' IDENT)? implementsdecl '{' class_body '}' ;
class_modifiers         : visibility_modifier? ( 'final' | 'abstract' )? ;
visbility_modifier      : 'public' | 'protected' | 'private' ;
//extends               : 'extends' ident_list ; // removido
implementsdecl          : 'implements' ident_list;

class_body              : ( attribute_decl | method_decl )* ;

attribute_decl          : type IDENT ';' | type IDENT '=' expr ';' ;
aloc_expr               : ;
expr                    : ( expr ) | arithmetic_expr | boolean_expr ;
arithmetic_expr         : expr ARITH_OP expr | STRING | number | INCREMENT_DECREMENT | IDENT ;
boolean_expr            : expr BOOLEAN_OP expr | BOOLEAN_VALUE | IDENT ;

method_decl             : 'def' IDENT '(' params ')' (':' type)? '{' method_body '}' ;
method_body             : 'not implemented yet';
params                  : type IDENT (',' type IDENT)* ;
args                    : ( IDENT | NULL ) ( ',' ( IDENT | NULL ))* ;

interface               : 'interface' IDENT '{' method_decl_abstract+ '}' ;
method_decl_abstract    : 'def' IDENT '(' params ')'? (':' IDENT)? ';' ;

enum                    : 'enum' IDENT '{' enum_body '}' ;
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
operator                :  ARITH_OP | BOOLEAN_OP ;
ARITH_OP                : '+' | '-' | '*' | '/' | '%' ;
BOOLEAN_OP              : '==' | '<' | '<=' | '>' | '>=' | 'or' | 'and' | 'is' ;
BOOLEAN_VALUE           : 'true' | 'false' ;
NULL                    : 'null' ;