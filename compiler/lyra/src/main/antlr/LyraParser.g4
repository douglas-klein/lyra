parser grammar LyraParser;
options{ tokenVocab=LyraLexer; }
program                 : importdecl* ( classdecl | interfacedecl | enumdecl )+ ;
importdecl              : IMPORT STRING SEMICOLON ;
classdecl               : class_modifiers CLASS IDENT extendsdecl? implementsdecl? LEFTCURLYBRACE class_body RIGHTCURLYBRACE ;
class_modifiers         : VISIBILITY_MODIFIER? ( FINAL | ABSTRACT )? ;
extendsdecl             : (EXTENDS IDENT);
implementsdecl          : IMPLEMENTS ident_list;
class_body              : ( attribute_decl SEMICOLON | method_decl )* ;
attribute_decl          : VISIBILITY_MODIFIER? type IDENT( LEFTBRACKET RIGHTBRACKET)* ( COMMA IDENT (LEFTBRACKET RIGHTBRACKET)* )* ( EQUALOP ( exprlist | aloc_expr ))?;
interfacedecl           : INTERFACE IDENT LEFTCURLYBRACE method_decl_abstract+ RIGHTCURLYBRACE ;

method_decl_abstract    : VISIBILITY_MODIFIER? DEF INFIX? IDENT (LEFTPARENTHESES params RIGHTPARENTHESES)? (COLON IDENT)? SEMICOLON ;

exprlist                : expr (COMMA expr)*;

/* O ANTLR 4, cf. Caps 5.4 e 14 do livro dele, suporta recursão esquerda direta
 * e usa por padrão uma estratégia de precedência baseada na ordem das
 * alternativas e assume sempre associatividade à esquerda
 * Isso mágicamente faz com que a regra abaixo, claramente ambígua
 * no semestre passado, funcione exatamente como desejado.
 */
expr                    : unaryexpr
                        | expr (MULTOP | SLASH | MODOP) expr
                        | expr (PLUS | MINUS) expr
                        | expr (LESSTHAN | LESSTHANOREQUAL | MORETHANOREQUAL | MORETHAN) expr
                        | expr (DOUBLEEQUALOP | NOTEQUAL | IS) expr
                        | expr AND expr
                        | expr OR expr
                        | expr IDENT expr
                        ;

unaryexpr               : ( NOT | PLUS | MINUS )? unaryexpr_2 ;
unaryexpr_2             : factor INCREMENT_DECREMENT?;
factor                  : NUMBER | STRING | NULL | lvalue | aloc_expr | BOOLEAN_VALUE
                        | LEFTPARENTHESES expr RIGHTPARENTHESES
                        | LEFTPARENTHESES expr {notifyErrorListeners("Unclosed '('");}
                        | LEFTPARENTHESES expr RIGHTPARENTHESES {notifyErrorListeners("Extra ')' after parenthised expression.");}
                        | LEFTPARENTHESES expr RIGHTPARENTHESES RIGHTPARENTHESES+ {notifyErrorListeners("Two or more extra ')' after parenthised expression.");}
                        ;

aloc_expr               : NEW ( IDENT LEFTPARENTHESES args RIGHTPARENTHESES | IDENT (LEFTBRACKET expr RIGHTBRACKET)+);
method_decl             : VISIBILITY_MODIFIER? DEF INFIX? IDENT (LEFTPARENTHESES params RIGHTPARENTHESES)? (COLON type)? LEFTCURLYBRACE method_body? RIGHTCURLYBRACE ;
method_body             : statlist;
param_decl              : IDENT COLON type
                        | type IDENT {notifyErrorListeners("Lyra expects parameters declared in the form \"name : type\"");}
                        ;
params                  : param_decl (COMMA param_decl)* ;
args                    : ( expr (COMMA expr)* )? ;
argsWs                  : expr+ ;
statement               : attribute_decl SEMICOLON
                        | atribstat  SEMICOLON
                        | returnstat SEMICOLON
                        | superstat  SEMICOLON
                        | ifstat
                        | forstat
                        | whilestat
                        | forever
                        | switchstat
                        | scopestat
                        | lvalue     SEMICOLON
                        | expr SEMICOLON
                        | BREAK SEMICOLON
                        | CONTINUE SEMICOLON
                        | SEMICOLON ;
scopestat               : LEFTCURLYBRACE statlist RIGHTCURLYBRACE ;
atribstat               : lvalue EQUALOP (aloc_expr | expr);
returnstat              : RETURN (expr)?;
superstat               : SUPER LEFTPARENTHESES args RIGHTPARENTHESES;
ifstat                  : IF  expr  LEFTCURLYBRACE statlist RIGHTCURLYBRACE elsestat? ;
elsestat                : ELSE LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
forstat                 : FOR attribute_decl?  SEMICOLON  expr SEMICOLON expr?  LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
whilestat               : WHILE expr LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
forever                 : FOREVER LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
switchstat              : SWITCH IDENT LEFTCURLYBRACE caselist RIGHTCURLYBRACE;
caselist                : casedecl (caselist)? | casedefault;
casedecl                : CASE expr COLON statlist ;
casedefault             : CASE DEFAULT COLON statlist ;
statlist                : statement (statlist)?;
lvalue                  : (IDENT | callOp) ( LEFTBRACKET expr RIGHTBRACKET | DOT IDENT (LEFTPARENTHESES args RIGHTPARENTHESES)?)*  ;
callOp                  : IDENT LEFTPARENTHESES args RIGHTPARENTHESES ;
enumdecl                : ENUM IDENT LEFTCURLYBRACE enum_body RIGHTCURLYBRACE ;
enum_body               : default_enum | named_enum ;
default_enum            : IDENT (COMMA IDENT) ;
named_enum              : IDENT EQUALOP ( STRING | NUMBER ) (SEMICOLON IDENT EQUALOP ( STRING | NUMBER ))* ;
type                    : IDENT ;
ident_list              : IDENT ( COMMA IDENT )* ;
