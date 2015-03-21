// Define a grammar called Hello
grammar Hello;
r  : 'hello' ID ;         // match keyword hello followed by an identifier
ID : [a-z]+ ;             // match lower-case identifiers
WS : [ \t\r\n]+ : skip ; // skip spaces, tabs, newlines



// Descrição (incompleta) da linguagem, utilizando a notação do ANTLR4 (até onde consegui entender)

program : import* ( class | interface | enum )+ ;
import : 'import' string ';' ;

class : class_modifiers 'class' ident extends? '{' class_body '}' ;
class_modifiers : visibility_modifier? ( 'final' | 'abstract' )? ;
visbility_modifier : 'public' | 'protected' | 'private' ;
extends : 'extends' ident_list ;

class_body : ( attribute_decl | method_decl )* ;

attribute_decl : type ident ';' | type ident '=' expr ';' ;
expr : ( expr ) | arithmetic_expr | boolean_expr ;
arithmetic_expr : expr arith_op expr | string | number | increment_decrement | ident ;
boolean_expr : expr boolean_op expr | boolean_value | ident ;

method_decl : 'def' ident '(' params ')' (':' type)? '{' method_body '}' ;

method_body : ###########################################################################

params : type ident (',' type ident)* ;
args : ( ident | null ) ( ',' ( ident | null ))* ;

interface : 'interface' ident '{' method_decl_abstract+ '}' ;
method_decl_abstract : 'def' ident '(' params ')'? (':' ident)? ';' ;
enum : 'enum' ident '{' enum_body '}' ;
enum_body : default_enum | named_enum ;
default_enum : ident (',' ident) ;
named_enum : ident '=' ( string | number ) (';' ident '=' ( string | number ))* ;

type : ident ;
ident_list : ident ( ',' ident )* ;
ident : [a-zA-Z_] ( [a-zA-Z_0-9] )* ;
string : '"' ~('"') '"' ;
number : ['+-']? ([0-9] | [1-9][0-9]*)( '.' [0-9]* )? ;
increment_decrement : ( '1'..'9')('0'..'9')*('++' | '--') ;
operator :  arith_op | boolean_op ;
arith_op : '+' | '-' | '*' | '/' | '%' ;
boolean_op : '==' | '<' | '<=' | '>' | '>=' | 'or' | 'and' | 'is' ;
boolean_value : 'true' | 'false' ;
null : 'null' ;