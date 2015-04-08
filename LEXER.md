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

- Exemplo de saída
```ruby
Int __b = 1747.98;
//NUMBER=53
//IDENT=51
//EQUALOP=36
//SEMICOLON=49

[@25,134:136='Int',<51>,9:8]
[@26,138:139='_b',<51>,9:12]
[@27,141:141='=',<36>,9:15]
[@28,143:149='1747.98',<53>,9:17]
[@29,150:150=';',<49>,9:24]
```

- Lexer aceita quase tudo
```ruby
Int __d = 0xff;
//NUMBER=53
//IDENT=51

[@30,235:237='Int',<51>,13:8]
[@31,239:241='__d',<51>,13:12]
[@32,243:243='=',<36>,13:16]
[@33,245:245='0',<53>,13:18]
[@34,246:248='xff',<51>,13:19]
```

### Interação com o código gerado
- Ações
- Listeners
- TokenFactory

