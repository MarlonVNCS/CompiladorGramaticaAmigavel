# Universidade Feevale

**Júlia Nathalie Schmitz**  
**Marlon Vinicius Gonçalves**  
**Gramática Amigável**  
**Compiladores**  
**Ciência da Computação**  
**01/2026**

## Introdução

Este trabalho foi desenvolvido por estudantes do curso de Ciência da Computação da Universidade Feevale, no contexto da disciplina de Compiladores, durante o primeiro semestre de 2026.
A gramática foi criada para a disciplina de Linguagens Formais e Autômatos no primeiro semestre de 2025.

## Compilador

O compilador foi desenvolvido em Java e contempla as etapas de análise léxica, sintática e semântica.
Ele compila a gramática criada por nós e gera código Python como saída.

## Observação: O arquivo principal do compilador é o Semantico.java, que contém a implementação das etapas de análise léxica, sintática, semântica e a geração do código de saída.

Os demais arquivos (ErroLexicoException.java, ErroSintaticoException.java, ErroSemanticoException.java, PilhaSemantica.java e NodoPilhaSemantica.java) são classes de apoio necessárias para a execução do compilador e não receberam modificações durante o desenvolvimento deste trabalho.

## Gramática

A gramática de programação foi projetada para iniciantes em lógica de programação, com foco no público jovem.
Para isso, foram adotados termos do português cotidiano a fim de facilitar a compreensão de conceitos fundamentais, tais como estruturas condicionais, laços de repetição e definição de funções, sem comprometer sua profundidade técnica.

A seguir serão detalhadas as características da gramática, incluindo seus componentes léxicos, as palavras reservadas e um exemplo de sentença válida. Para a sua representação formal, foi utilizado o Formalismo de Backus-Naur Estendido (EBNF).

## Palavras reservadas da linguagem

| Inicio: | palavra | se_nao | maior |
|--------|--------|--------|-------|
| :Acabou | vem | escrever | menor |
| faz | sera_que | de | igual |
| num | e_se | ate | |

## Termos léxicos

### Letras e dígitos

```ebnf
LETRA := 'a' | ... | 'z' | 'A' | ... | 'Z' ;
DIGITO := '0' | '1' | ... | '9' ;
```

### Número e string

```ebnf
NUMERO ::= DIGITO {DIGITO} 
        | DIGITO '.' {DIGITO};

PALAVRA_STRING ::= '"' { qualquer_caractere_ou_interpolacao } '"' ;
```

### Identificador

```ebnf
IDENTIFICADOR := LETRA { LETRA | DIGITO | '_' } ;
```

### Comentários

```ebnf
COMENTARIO := '//' {caractere} 
            | '/*' {qualquer_caractere} '*/' ;
```

### Operadores e símbolos

```ebnf
OPERADOR := '+' | '-' | '*' | '/' ;
COMPARADOR := 'maior' | 'menor' | 'igual' | 'maior igual' | 'menor igual' ;
SIMBOLO ::= ':' | '(' | ')' | '{' | '}' | ',' | '=' ;
```

## Gramática

```ebnf
programa := 'Inicio:' bloco ':Acabou' ;

bloco := { declaracao | comando | COMENTARIO } ;

declaracao ::= tipo ':' IDENTIFICADOR '=' expressao
             | tipo ':' IDENTIFICADOR ;

tipo := 'num' | 'palavra' ;

comando := atribuicao
         | chamada_funcao
         | condicional
         | repeticao
         | saida
         | retorno
         | definicao_funcao ;

atribuicao ::= tipo ':' IDENTIFICADOR '=' expressao ;

expressao ::= NUMERO
            | PALAVRA_STRING
            | IDENTIFICADOR
            | expressao OPERADOR expressao
            | 'vem' IDENTIFICADOR '(' argumentos ')'
            | 'vem' IDENTIFICADOR '(' ')'
            | '{' expressao '}' ;

argumentos := expressao { ',' expressao } ;

saida ::= 'escrever' '(' expressao ')' ;
retorno := 'retorna' '(' expressao ')' ;

definicao_funcao ::= 'faz' tipo ':' IDENTIFICADOR '(' parametros ')' '{' bloco '}'
                   | 'faz' tipo ':' IDENTIFICADOR '(' ')' '{' bloco '}' ;

parametros ::= tipo ':' IDENTIFICADOR { ',' tipo ':' IDENTIFICADOR } ;

condicional := 'sera_que' '(' condicao ')' '{' bloco '}'
             { 'e_se' '(' condicao ')' '{' bloco '}' }
             [ 'se_nao' '{' bloco '}' ] ;

condicao ::= expressao COMPARADOR expressao ;

repeticao ::= 'ciclo' '(' tipo ':' IDENTIFICADOR 'de' expressao 'ate' expressao ')' '{' bloco '}' ;
```

## Exemplo de string aceita pela gramática

```text
Inicio:

faz num:soma(num : n1, num : n2){
num : resultado = n1 + n2
retorna(resultado)
}

num : n1 = 10
num : n2 = 5
num : n3 = vem soma(n1, n2)

sera_que(n3 maior igual 10){
escrever("Maior ou igual a 10")
}e_se(n3 menor 5){
escrever("Menor que 5")
}se_nao{
escrever("Nenhum")
}

escrever("Todos os números até {n3}")

ciclo(num:i de 0 ate n3){
escrever(i)
}

:Acabou
```

## Conclusão

A Gramática Amigável mistura inspirações da G-Portugol com elementos do TypeScript, adaptando tudo em uma sintaxe focada na simplicidade e no fácil entendimento. Ao usar termos do português cotidiano, o projeto cumpre seu objetivo principal de tornar a programação mais acessível. Dessa forma, a gramática funciona como um ótimo ponto de partida para iniciantes, especialmente crianças e jovens, permitindo um primeiro contato com os conceitos fundamentais da lógica de maneira clara e acolhedora.
