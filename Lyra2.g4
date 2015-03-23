grammar Lyra;

program                 : WS* importdecl* ( classdecl | interfacedecl | enumdecl )+ WS* ;
importdecl              : 'import' WS* STRING WS* ';' WS* ;
classdecl               : WS* class_modifiers WS* 'class' WS* IDENT WS* ('extends' WS* IDENT WS*)? implementsdecl? WS*'{'WS* class_body WS*'}'WS* ;
class_modifiers         : visbility_modifier? WS* ( 'final' | 'abstract' )? ;
visbility_modifier      : 'public' | 'protected' | 'private' ;
implementsdecl          : 'implements' WS* ident_list;
class_body              : ( WS* attribute_decl WS*';'WS* | WS* method_decl WS* )* ;
attribute_decl          : type WS* IDENT('[]')* ( WS* ',' WS* IDENT ('[]')* )* (WS*'='WS* ( exprlist | aloc_expr ))?;

interfacedecl           : WS* 'interface' WS* IDENT WS*'{'WS* method_decl_abstract+ WS*'}'WS* ;
method_decl_abstract    : 'def' WS* IDENT (WS*'('WS* params WS*')'WS*)? (WS*':'WS* IDENT)? WS*';'WS* ;
enumdecl                : 'enum' WS* IDENT WS*'{'WS* enum_body WS*'}'WS* ;
enum_body               : default_enum | named_enum ;
default_enum            : IDENT ( WS*','WS* IDENT) ;
named_enum              : IDENT WS*'='WS* ( STRING | NUMBER ) ( WS*';'WS* IDENT WS*'='WS* ( STRING | NUMBER ))* ;

exprlist                : expr ( WS*','WS* expr)*;
expr                    : numexp  ( WS*( '>' | '<' | '>=' | '<=' | '==' | '!=' | 'or' | 'and' | 'is')WS* numexp)?;
numexp                  : term  ( WS*('+' | '-')WS* term )* ;
term                    : unaryexp (WS*('*' | '/' | '%' )WS* unaryexp)*;
unaryexp                : ('+' | '-' | '!' )?WS* factor (WS* INCREMENT_DECREMENT WS*)?; // ok
factor                  : NUMBER | STRING | NULL | lvalue | WS*'('WS* expr  WS*')'WS*;

aloc_expr               : WS*'new'WS* (IDENT args? | IDENT ('['WS* expr  WS*']'WS*)+)  ( WS*','WS*'new'WS* (IDENT args? | IDENT ('['WS* expr WS*']'WS*)+))*;

method_decl             : 'def'WS* IDENT (WS*'('WS* params WS*')'WS*)? ( WS*':'WS* type)? WS*'{'WS* method_body? WS*'}'WS*;
method_body             : statlist;
params                  : type WS* IDENT ( WS*','WS* type WS* IDENT)* ;
args                    : (WS*'('WS* argslist WS*')'WS* | WS argslistWS);
argslist                : ( expr ( WS*','WS* expr)* )? ;
argslistWS              : ( expr ( WS expr)*)? ;

statement               :
                        ( attribute_decl
                        | atribstat  WS*';' WS*
                        | returnstat WS*';'WS*
                        | superstat  WS*';'WS*
                        | ifstat
                        | forstat
                        | whilestat
                        | switchstat
                        | WS*'{'WS* statlist WS*'}'WS*
                        | lvalue  WS*';'WS*
                        | expr    WS*';'WS*
                        | 'break' WS*';'WS*
                        | WS*';'WS* ) ;

atribstat               : lvalue WS*'='WS* (aloc_expr | expr);
returnstat              : 'return'WS* (expr)?;
superstat               : 'super'  args ;
statlist                : statement WS* (statlist)?;
lvalue                  : (IDENT | callOp) ( '['WS*  expr WS*']'WS* | WS*'.'WS* IDENT args?)*  ;
callOp                  : IDENT args;

ifstat                  : 'if'WS* expr  WS* statlist ( WS*'else'WS*  statlist)? ;
forstat                 : 'for'WS*  ( WS* attribute_decl WS*';'WS*) (expr WS*';'WS*) expr  statlist;
whilestat               : 'while'WS* expr? WS* statlist;
switchstat              : 'switch'WS* IDENT WS*'{'WS*  caselist WS*'}'WS*;
caselist                : casedecl  ( WS* caselist)? WS* | casedefault;
casedecl                : 'case'WS* expr WS*':'WS* statlist ;
casedefault             : 'case'WS*'default'WS*':'WS* statlist ;

type                    : ('int' | 'string' | IDENT);
ident_list              : IDENT ( WS*','WS* IDENT )* ;
IDENT                   : [a-zA-Z] [a-zA-Z_0-9]* ;
STRING                  : '"' ( '\\"' | . )*? '"' ;
NUMBER                  : ([0-9] | [1-9][0-9]*)( '.' [0-9]* )? ;
INCREMENT_DECREMENT     : '++' | '--' ;
BOOLEAN_VALUE           : 'true' | 'false' ;
NULL                    : 'null' ;
COMMENT                 : '/*' .*? '*/' -> skip ; // .*? matches anything until the first */
LINECOMMENT             : '//' .*? ('\r' | '\n') -> skip ;
WS                      : [ \t\r\n];
//WS                      : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines