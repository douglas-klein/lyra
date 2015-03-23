grammar Lyra;

program                 : importdecl* ( classdecl | interfacedecl | enumdecl )+ ;
importdecl              : 'import' STRING ';' ;
classdecl               : class_modifiers 'class' IDENT ('extends' IDENT)? implementsdecl? '{' class_body '}' ;
class_modifiers         : visbility_modifier? ( 'final' | 'abstract' )? ;
visbility_modifier      : 'public' | 'protected' | 'private' ;
implementsdecl          : 'implements' ident_list;
class_body              : ( attribute_decl ';' | method_decl )* ;
attribute_decl          : type IDENT('[]')* ( ',' IDENT ('[]')* )* ('=' ( exprlist | aloc_expr ))?;

// Expressões de operadores da forma ...1+1 ... 2*2 ... a+2 ... b*(c+a) ... b>(6/3) .. etc
exprlist                : expr (',' expr)*;
expr                    : expr_2 expr_opt;
expr_opt                : IDENT expr_2 expr_opt | ;
expr_2                  : expr_3 expr_2_opt ;
expr_2_opt              : 'or' expr_3 expr_2_opt | ;
expr_3                  : expr_4 expr_3_opt ;
expr_3_opt              : 'and' expr_4 expr_3_opt | ;
expr_4                  : expr_5 expr_4_opt ;
expr_4_opt              : 'is' expr_5 expr_4_opt | ;
expr_5                  : expr_6 expr_5_opt ;
expr_5_opt              : ('==' | '!=') expr_6 expr_5_opt | ;
expr_6                  : expr_7 expr_6_opt ;
expr_6_opt              : ('<' | '<=' | '>=' | '>') expr_7 expr_6_opt| ;
expr_7                  : expr_8 expr_7_opt ;
expr_7_opt              : ('+' | '-') expr_8 expr_7_opt | ;
expr_8                  : unaryexpr expr_8_opt ;
expr_8_opt              : ('*' | '/' | '%') unaryexpr expr_8_opt | ;
unaryexpr               : ('!' | '+' | '-')? unaryexpr_2 ;
unaryexpr_2             : factor INCREMENT_DECREMENT?;
factor                  : NUMBER | STRING | NULL | lvalue | aloc_expr | '(' expr ')';

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
                        | expr ';'
                        | 'break' ';'
                        | ';') ;
atribstat               : lvalue '=' (aloc_expr | expr);
returnstat              : 'return' (expr)?;
superstat               : 'super' '(' args ')';
ifstat                  : 'if'  expr  statement ( 'else' statement)? ;
forstat                 : 'for' attribute_decl? ';' expr? ';' expr?  statement;
whilestat               : 'while' expr? statement;
switchstat              : 'switch' IDENT '{' caselist '}';
caselist                : casedecl (caselist)? | casedefault;
casedecl                : 'case' expr ':' statement ;
casedefault             : 'case' 'default' ':' statement ;
statlist                : statement (statlist)?;
lvalue                  : (IDENT | callOp) ( '[' expr ']' | '.' IDENT ('(' args ')')?)*  ;
callOp                  : IDENT '(' args ')' ;
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
INCREMENT_DECREMENT     : ('++' | '--') ;
BOOLEAN_VALUE           : 'true' | 'false' ;
NULL                    : 'null' ;
COMMENT                 : '/*' .*? '*/' -> skip ; // .*? matches anything until the first */
LINECOMMENT             : '//' .*? ('\r' | '\n') -> skip ;
WS                      : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
