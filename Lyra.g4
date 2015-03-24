grammar Lyra;

program                 : importdecl* ( classdecl | interfacedecl | enumdecl )+ ;
importdecl              : 'import' STRING ';' ;
classdecl               : class_modifiers 'class' IDENT ('extends' IDENT)? implementsdecl? '{' class_body '}' ;
class_modifiers         : visibility_modifier? ( 'final' | 'abstract' )? ;
visibility_modifier     : 'public' | 'protected' | 'private' ;
implementsdecl          : 'implements' ident_list;
class_body              : ( attribute_decl  | method_decl )* ;
attribute_decl          : visibility_modifier? type IDENT('[]')* ( ',' IDENT ('[]')* )* ('=' ( exprlist | aloc_expr ))?;
interfacedecl           : 'interface' IDENT '{' method_decl_abstract+ '}' ;

//Métodos declarados dessa maneira são sempre abstract, não podem ser 
//implementados nessa classe e devem ser implementados por alguma classe filha
method_decl_abstract    : visibility_modifier? 'def' 'infix'? IDENT ('(' params ')')? (':' IDENT)? ';' ;


// Expressões de operadores da forma ...1+1 ... 2*2 ... a+2 ... b*(c+a) ... b>(6/3) .. etc
exprlist                : expr (',' expr)*;
expr                    : expr_2 expr_opt;
expr_opt                : IDENT expr_2 expr_opt | ;
expr_2                  : expr_3 expr_2_opt ;
expr_2_opt              : 'or' expr_3 expr_2_opt | ;
expr_3                  : expr_4 expr_3_opt ;
expr_3_opt              : 'and' expr_4 expr_3_opt | ;
expr_4                  : expr_5 expr_4_opt ;
expr_4_opt              : ('==' | '!=' | 'is') expr_5 expr_4_opt | ;
expr_5                  : expr_6 expr_5_opt ;
expr_5_opt              : ('<' | '<=' | '>=' | '>') expr_6 expr_5_opt| ;
expr_6                  : expr_7 expr_6_opt ;
expr_6_opt              : ('+' | '-') expr_7 expr_6_opt | ;
expr_7                  : unaryexpr expr_7_opt ;
expr_7_opt              : ('*' | '/' | '%') unaryexpr expr_7_opt | ;
unaryexpr               : ('!' | '+' | '-')? unaryexpr_2 ;
unaryexpr_2             : factor INCREMENT_DECREMENT?;
factor                  : NUMBER | STRING | NULL | lvalue | aloc_expr | BOOLEAN_VALUE | '(' expr ')';

aloc_expr               : 'new' ( IDENT '(' args ')' | IDENT ('[' expr ']')+);
method_decl             : visibility_modifier? 'def' 'infix'? IDENT ('(' params ')')? (':' type)? '{' method_body? '}' ;
method_body             : statlist;
params                  : type IDENT (',' type IDENT)* ;
args                    : ( expr (',' expr)* )? ;
argsWs                  : expr+ ;
statement               :
                        ( attribute_decl ';'
                        | atribstat  ';'
                        | returnstat ';'
                        | superstat  ';'
                        | ifstat
                        | forstat
                        | whilestat
                        | forever
                        | switchstat
                        | '{' statlist '}'
                        | lvalue     ';'
                        | expr ';'
                        | 'break' ';'
                        | ';') ;
atribstat               : lvalue '=' (aloc_expr | expr);
returnstat              : 'return' (expr)?;
superstat               : 'super' '(' args ')';
ifstat                  : 'if'  expr  statlist ( 'else' statlist)? ;
forstat                 : 'for' attribute_decl?  ';'  expr ';' expr?  statlist;
whilestat               : 'while' expr statlist;
forever                 : 'forever' statlist;
switchstat              : 'switch' IDENT '{' caselist '}';
caselist                : casedecl (caselist)? | casedefault;
casedecl                : 'case' expr ':' statlist ;
casedefault             : 'case' 'default' ':' statlist ;
statlist                : statement (statlist)?;
lvalue                  : (IDENT | callOp) ( '[' expr ']' | '.' IDENT ('(' args ')')?)*  ;
callOp                  : IDENT '(' args ')' ;
enumdecl                : 'enum' IDENT '{' enum_body '}' ;
enum_body               : default_enum | named_enum ;
default_enum            : IDENT (',' IDENT) ;
named_enum              : IDENT '=' ( STRING | NUMBER ) (';' IDENT '=' ( STRING | NUMBER ))* ;
type                    : IDENT ;
ident_list              : IDENT ( ',' IDENT )* ;
IDENT                   : [a-zA-Z_] [a-zA-Z_0-9]* ;
STRING                  : '"' ( '\\"' | . )*? '"' ;
NUMBER                  : ([0-9] | [1-9][0-9]*)( '.' [0-9]+ )? ;
INCREMENT_DECREMENT     : ('++' | '--') ;
BOOLEAN_VALUE           : 'true' | 'false' ;
NULL                    : 'null' ;
COMMENT                 : '/*' .*? '*/' -> skip ; // .*? matches anything until the first */
LINECOMMENT             : '//' .*? ('\r' | '\n') -> skip ;
WS                      : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
