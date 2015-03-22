#!/bin/sh
# Testado apenas no bash
 
# Truque rápido para converter da sintaxe do ANTLR 4 (arquivos .g4) para a 
# sintaxe EBNF assim como aceita pelo gerador de diagramas de sintaxe em
# http://www.bottlecaps.de/rr/ui
# 
# Note que como só expressões regulares estão sendo usadas, algumas coisas 
# Não são removidas corretamente:
# - Comandos "-> cmds"  onde cmds é qualquer coisa diferente de uma única 
#   palavra
# - Expressões regulares como [ \t\n\r]+
#
# Linhas em branco, comentários e o nome da gramática são removidos
#
# Uso: to_ebnf.sh Lyra.g4 Lyra.ebnf

sed -r -e "s;'/\*';GAMBIARRA_SALVA_ABRE_COMMENT;" \
       -e "s;'\*/';GAMBIARRA_SALVA_FECHA_COMMENT;" \
       -e "s;'//';GAMBIARRA_SALVA_LINE_COMMENT;" < "$1" \
       | sed -r -e 's;//.*$;;' \
                -e 's;/\*.*\*/;;' \
                -e 's/grammar.*;//' \
                -e 's/->\s*\w+//' \
                -e 's/^([a-zA-Z0-9_\-]+\s+):/\1::=/' \
       | sed -r 's/\s*;\s*$//' \
       | sed -r '/^\s*$/ d' \
       | sed -r -e "s;GAMBIARRA_SALVA_ABRE_COMMENT;'/*';" \
                -e "s;GAMBIARRA_SALVA_FECHA_COMMENT;'*/';" \
		-e "s;GAMBIARRA_SALVA_LINE_COMMENT;'//';" > "$2"
