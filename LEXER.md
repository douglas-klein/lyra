# LyraLexer

### Lexers com ANTLR4
```ruby
lexer grammar LyraLexer;
```
- Ambiguidade léxica resolvida aceitando a primeira regra
  - IDENT é uma das últimas regas
- Parser referencia LyraLexer:
```ruby
parser grammar Lyra;
options{ tokenVocab=LyraLexer; }
```

### LyraLexer
- Comentários e espaços ignorados pelo lexer:
```ruby
COMMENT                 : '/*' .*? '*/' -> skip ;
LINECOMMENT             : '//' .*? ('\r' | '\n') -> skip ;
WS                      : [ \t\r\n]+ -> skip ;
```

- Muitos literais
```ruby
FOR                     : 'for';
IF                      : 'if';
ELSE                    : 'else';
VISIBILITY_MODIFIER     : 'public' | 'protected' | 'private' ;
RIGHTCURLYBRACE         : '}';
LEFTCURLYBRACE          : '{';
CLASS                   : 'class' ;
AND                     : 'and';
OR                      : 'or';
NOTEQUAL                : '!=';
SEMICOLON               : ';' ;
```

- Regras genéricas no final do arquivo
```ruby
IDENT                   : [a-zA-Z_] [a-zA-Z_0-9]* ;
STRING                  : '"' ( '\\"' | . )*? '"' ;
NUMBER                  : ([0-9] | [1-9][0-9]*)( '.' [0-9]+ )? ;
```

### Interação com o código gerado
- Listeners
- TokenFactory
