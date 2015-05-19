parser grammar LyraParser;
options{ tokenVocab=LyraLexer; }
program                 : importdecl* ( classdecl | interfacedecl | enumdecl )+ ;
importdecl              : IMPORT STRING SEMICOLON ;
classdecl               : classModifiers CLASS IDENT extendsdecl? implementsdecl? LEFTCURLYBRACE classBody RIGHTCURLYBRACE ;
classModifiers          : VISIBILITYMODIFIER? ( FINAL | ABSTRACT )? ;
extendsdecl             : (EXTENDS IDENT);
implementsdecl          : IMPLEMENTS identList;
classBody               : ( attributeDecl SEMICOLON | methodDecl )* ;

attributeDecl           : VISIBILITYMODIFIER? varDecl;
varDecl                 : type varDeclUnit (COMMA varDeclUnit)* (EQUALOP exprlist )? ;
varDeclUnit             : IDENT arrayDeclSuffix*;
arrayDeclSuffix         : LEFTBRACKET RIGHTBRACKET;

interfacedecl           : INTERFACE IDENT LEFTCURLYBRACE methodDeclAbstract+ RIGHTCURLYBRACE ;

methodDeclAbstract      : VISIBILITYMODIFIER? DEF INFIX? IDENT (LEFTPARENTHESES params RIGHTPARENTHESES)? (COLON type)? SEMICOLON ;

exprlist                : expr (COMMA expr)*;

/* O ANTLR 4, cf. Caps 5.4 e 14 do livro dele, suporta recursão esquerda direta
 * e usa por padrão uma estratégia de precedência baseada na ordem das
 * alternativas e assume sempre associatividade à esquerda
 * Isso mágicamente faz com que a regra abaixo, claramente ambígua
 * no semestre passado, funcione exatamente como desejado.
 */
expr                    : unaryexpr
                        | expr  binOp=(MULTOP | SLASH | MODOP) expr
                        | expr  binOp=(PLUS | MINUS) expr
                        | expr  binOp=(LESSTHAN | LESSTHANOREQUAL | MORETHANOREQUAL | MORETHAN) expr
                        | expr  binOp=(DOUBLEEQUALOP | NOTEQUAL | IS) expr
                        | expr  binOp=AND expr
                        | expr  binOp=OR expr
                        | expr  binOp=IDENT expr
                        | IDENT binOp=EQUALOP expr
                        ;

unaryexpr               : factor
                        | prefixOp=( NOT | PLUS | MINUS ) unaryexpr
                        | unaryexpr postfixOp=(INCREMENT | DECREMENT)
                        ;

factor                  : factor DOT IDENT ( LEFTPARENTHESES args RIGHTPARENTHESES )?  # memberFactor
                        | IDENT ( LEFTPARENTHESES args RIGHTPARENTHESES )              # thisMethodFactor
                        | factor LEFTBRACKET expr RIGHTBRACKET                         # arrayFactor
                        | NUMBER                                # numberFactor
                        | STRING                                # stringFactor
                        | NULL                                  # nullFactor
                        | IDENT                                 # nameFactor
                        | alocExpr                              # newfactor
                        | BOOLEANVALUE                          # boolFactor
                        | LEFTPARENTHESES expr RIGHTPARENTHESES # wrappedFactor
                        | LEFTPARENTHESES expr {notifyErrorListeners("Unclosed '('");}  # wrong1WrappedFactor
                        | LEFTPARENTHESES expr RIGHTPARENTHESES RIGHTPARENTHESES+ {notifyErrorListeners("Extra ')'s after parenthised expression.");} # wrong2WrappedFactor
                        ;

alocExpr                : NEW ( IDENT LEFTPARENTHESES args RIGHTPARENTHESES | IDENT (LEFTBRACKET expr RIGHTBRACKET)+);

methodDecl              : VISIBILITYMODIFIER? DEF INFIX? IDENT (LEFTPARENTHESES params RIGHTPARENTHESES)? (COLON type)? LEFTCURLYBRACE methodBody? RIGHTCURLYBRACE ;
methodBody              : statlist;
paramDecl               : IDENT COLON type
                        | type IDENT {notifyErrorListeners("Lyra expects parameters declared in the form \"name : type\"");}
                        ;
params                  : paramDecl (COMMA paramDecl)* ;
args                    : ( expr (COMMA expr)* )? ;
argsWs                  : expr+ ;
statement               : varDecl SEMICOLON
                        | returnstat SEMICOLON
                        | superstat  SEMICOLON
                        | ifstat
                        | forstat
                        | whilestat
                        | forever
                        | switchstat
                        | scopestat
                        | expr SEMICOLON
                        | BREAK SEMICOLON
                        | CONTINUE SEMICOLON
                        | SEMICOLON ;
scopestat               : LEFTCURLYBRACE statlist RIGHTCURLYBRACE ;
returnstat              : RETURN (expr)?;
superstat               : SUPER LEFTPARENTHESES args RIGHTPARENTHESES;
ifstat                  : IF  expr  LEFTCURLYBRACE statlist RIGHTCURLYBRACE elsestat? ;
elsestat                : ELSE LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
forstat                 : FOR varDecl?  SEMICOLON  expr SEMICOLON expr?  LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
whilestat               : WHILE expr LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
forever                 : FOREVER LEFTCURLYBRACE statlist RIGHTCURLYBRACE;
switchstat              : SWITCH IDENT LEFTCURLYBRACE caselist RIGHTCURLYBRACE;
caselist                : casedecl (caselist)? | casedefault;
casedecl                : CASE expr COLON statlist ;
casedefault             : CASE DEFAULT COLON statlist ;
statlist                : statement (statlist)?;
enumdecl                : ENUM IDENT LEFTCURLYBRACE enumBody RIGHTCURLYBRACE ;
enumBody                : defaultEnum | namedEnum ;
defaultEnum             : IDENT (COMMA IDENT) ;
namedEnum               : IDENT EQUALOP ( STRING | NUMBER ) (SEMICOLON IDENT EQUALOP ( STRING | NUMBER ))* ;
type                    : IDENT ;
identList               : IDENT ( COMMA IDENT )* ;
