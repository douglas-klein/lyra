parser grammar Lyra;

options { tokenVocab=LyraLexer; }

program                 : importdecl* ( classdecl | interfacedecl | enumdecl )+ ;
importdecl              : IMPORT STRING COLON ;
classdecl               : class_modifiers 'class' IDENT (EXTENDS IDENT)? implementsdecl? LEFTCURLYBRACE class_body RIGHTCURLYBRACE ;
class_modifiers         : VISIBILITY_MODIFIER? ( FINAL | ABSTRACT )? ;
implementsdecl          : IMPLEMENTS ident_list;
class_body              : ( attribute_decl  | method_decl )* ;
attribute_decl          : VISIBILITY_MODIFIER? type IDENT( LEFTBRACKET RIGHTBRACKET)* ( COMMA IDENT (LEFTBRACKET RIGHTBRACKET)* )* ( EQUALOP ( exprlist | aloc_expr ))?;
interfacedecl           : INTERFACE IDENT LEFTCURLYBRACE method_decl_abstract+ RIGHTCURLYBRACE ;

method_decl_abstract    : VISIBILITY_MODIFIER? DEF INFIX? IDENT (LEFTPARENTHESES params RIGHTPARENTHESES)? (COLON IDENT)? SEMICOLON ;

exprlist                : expr (COMMA expr)*;
expr                    : expr_2 expr_opt;
expr_opt                : IDENT expr_2 expr_opt | ;
expr_2                  : expr_3 expr_2_opt ;
expr_2_opt              : OR expr_3 expr_2_opt | ;
expr_3                  : expr_4 expr_3_opt ;
expr_3_opt              : AND expr_4 expr_3_opt | ;
expr_4                  : expr_5 expr_4_opt ;
expr_4_opt              : (DOUBLEEQUALOP | NOTEQUAL | IS) expr_5 expr_4_opt | ;
expr_5                  : expr_6 expr_5_opt ;
expr_5_opt              : (LESSTHAN | LESSTHANOREQUAL | MORETHANOREQUAL | MORETHAN) expr_6 expr_5_opt| ;
expr_6                  : expr_7 expr_6_opt ;
expr_6_opt              : ( PLUS | MINUS) expr_7 expr_6_opt | ;
expr_7                  : unaryexpr expr_7_opt ;
expr_7_opt              : (MULTOP | SLASH | MODOP) unaryexpr expr_7_opt | ;
unaryexpr               : ( NOT | PLUS | MINUS )? unaryexpr_2 ;
unaryexpr_2             : factor INCREMENT_DECREMENT?;
factor                  : NUMBER | STRING | NULL | lvalue | aloc_expr | BOOLEAN_VALUE | LEFTPARENTHESES expr RIGHTPARENTHESES;

aloc_expr               : NEW ( IDENT LEFTPARENTHESES args RIGHTPARENTHESES | IDENT (LEFTBRACKET expr RIGHTBRACKET)+);
method_decl             : VISIBILITY_MODIFIER? DEF INFIX? IDENT (LEFTPARENTHESES params RIGHTPARENTHESES)? (COLON type)? LEFTBRACKET method_body? RIGHTBRACKET ;
method_body             : statlist;
params                  : type IDENT (COMMA type IDENT)* ;
args                    : ( expr (COMMA expr)* )? ;
argsWs                  : expr+ ;
statement               :
                        ( attribute_decl SEMICOLON
                        | atribstat  SEMICOLON
                        | returnstat SEMICOLON
                        | superstat  SEMICOLON
                        | ifstat
                        | forstat
                        | whilestat
                        | forever
                        | switchstat
                        | LEFTCURLYBRACE statlist RIGHTCURLYBRACE
                        | lvalue     SEMICOLON
                        | expr SEMICOLON
                        | BREAK SEMICOLON
                        | CONTINUE SEMICOLON
                        | SEMICOLON) ;
atribstat               : lvalue EQUALOP (aloc_expr | expr);
returnstat              : RETURN (expr)?;
superstat               : SUPER LEFTPARENTHESES args RIGHTPARENTHESES;
ifstat                  : IF  expr  LEFTCURLYBRACE statlist RIGHTCURLYBRACE ( ELSE LEFTCURLYBRACE statlist RIGHTCURLYBRACE)? ;
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

