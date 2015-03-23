grammar Lyra;

program                 : importdecl* ( classdecl | interfacedecl | enumdecl )+ ;
importdecl              : 'import' STRING ';' ;
classdecl               : class_modifiers 'class' IDENT ('extends' IDENT)? implementsdecl? '{' class_body '}' ;
class_modifiers         : visbility_modifier? ( 'final' | 'abstract' )? ;
visbility_modifier      : 'public' | 'protected' | 'private' ;
implementsdecl          : 'implements' ident_list;
class_body              : ( attribute_decl | method_decl )* ;
// Declarações podem ser do tipo Int a,b,c; ... ou ...  Int a = 1; ... ou ... Int a,b,c = 1,2,3;
// OBS : A forma ... Int a,b,c = 1,2,3; ainda não funciona na nosas gramática
attribute_decl          : type IDENT('[' ']')* (',' IDENT ('[' ']')* )* ';'
                        | type IDENT('[' ']')* (',' IDENT ('[' ']')* )* '=' ( expr | aloc_expr )';' ;

// Expressões de operadores da forma ...1+1 ... 2*2 ... a+2 ... b*(c+a) ... b>(6/3) .. etc
expr                    : expr_2 ( IDENT expr_2)? ;
expr_2                  : expr_3 ( 'or' expr_3)? ;
expr_3                  : expr_4 ( 'and' expr_4)? ;
expr_4                  : expr_5 ( 'is' expr_5)? ;
expr_5                  : expr_6 ( ('==' | '!=') expr_6)? ;
expr_6                  : expr_7 ( ('<' | '<=' | '>=' | '>') expr_7)? ;
expr_7                  : expr_8 ( ('+' | '-') expr_8)? ;
expr_8                  : unaryexpr ( ('*' | '/' | '%') unaryexpr)? ;
unaryexpr               : ('!' | '+' | '-')? factor ;
factor                  : NUMBER | STRING | NULL | lvalue | '(' expr ')';

// Expressoes de alocação de objetos e arrays da forma ... " new Dog() " ... " new Point(2,2) ... " new Int[10] "
aloc_expr               : 'new' ( IDENT '(' args ')' | IDENT ('[' expr ']')+);
method_decl             : 'def' IDENT ('(' params ')')? (':' type)? '{' method_body? '}' ;
method_body             : statlist;
params                  : type IDENT (',' type IDENT)* ;
args                    : ( expr (',' expr)* )? ;
argsWs                  : expr+ ;
statement               :
                        ( attribute_decl
                        | atribstat  ';'
                        | returnstat ';'
                        | superstat  ';'
                        | ifstat
                        | forstat
                        | whilestat
                        | switchstat
                        | '{' statlist '}'
                        | lvalue     ';'
                        | 'break' ';'
                        | ';') ;
atribstat               : lvalue '=' (aloc_expr | expr);
returnstat              : 'return' (expr)?;
superstat               : 'super' '(' args ')';
ifstat                  : 'if'  expr  statement ( 'else' statement)? ;
forstat                 : 'for' atribstat? ';' expr? ';' atribstat?  statement;
whilestat               : 'while' expr? statement;
switchstat              : 'switch' IDENT '{' caselist '}';
caselist                : casedecl (caselist)? | casedefault;
casedecl                : 'case' expr ':' statement ;
casedefault             : 'case' 'default' ':' statement ;
statlist                : statement (statlist)?;
lvalue                  : (IDENT | callOp) ( '[' expr ']' | '.' IDENT ('(' args ')')?)*  ;
callOp                  : 
                        ( IDENT '(' args ')'
                        | IDENT argsWs );
interfacedecl           : 'interface' IDENT '{' method_decl_abstract+ '}' ;
method_decl_abstract    : 'def' IDENT '(' params ')'? (':' IDENT)? ';' ;
enumdecl                : 'enum' IDENT '{' enum_body '}' ;
enum_body               : default_enum | named_enum ;
default_enum            : IDENT (',' IDENT) ;
named_enum              : IDENT '=' ( STRING | NUMBER ) (';' IDENT '=' ( STRING | NUMBER ))* ;
type                    : IDENT ;
ident_list              : IDENT ( ',' IDENT )* ;
IDENT                   : [a-zA-Z_] [a-zA-Z_0-9]* ;
STRING                  : '"' ( '\\"' | . )*? '"' ;
NUMBER                  : ([0-9] | [1-9][0-9]*)( '.' [0-9]* )? ;
INCREMENT_DECREMENT     : ('1'..'9')('0'..'9')*('++' | '--') ;
BOOLEAN_VALUE           : 'true' | 'false' ;
NULL                    : 'null' ;
COMMENT                 : '/*' .*? '*/' -> skip ; // .*? matches anything until the first */
LINECOMMENT             : '//' .*? ('\r' | '\n') -> skip ;
WS                      : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
