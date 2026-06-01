import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

/**
 * Semantico - Primeira versao do semantico
 * 
 * @author Ricardo Ferreira de Oliveira
 * @author Turma de projeto de compiladores 1/2023
 *

gramatica:

<G> ::= 'PROGRAMA' <LISTA> <CMDS> 'FIM'
<LISTA> ::= 'VARIAVEIS' <VARS> ';'
<VARS> ::= <VAR> , <VARS>
<VARS> ::= <VAR> 
<VAR>  ::= <ID>
<CMDS> ::= <CMD> ; <CMDS>
<CMDS> ::= <CMD>
<CMD> ::= <CMD_SE>
<CMD> ::= <CMD_ENQUANTO>
<CMD> ::= <CMD_PARA>
<CMD> ::= <CMD_ATRIBUICAO>
<CMD> ::= <CMD_LER>
<CMD> ::= <CMD_ESCREVER>
<CMD> ::= <CMD_FACA>
<CMD> ::= <CMD_REPITA>
<CMD> ::= <CMD_CASO>
<CMD> ::= <CMD_FATORIAL>
<CMD_SE> ::= 'SE' '(' <CONDICAO> ')' <CMDS> 'FIM_SE' 
<CMD_SE> ::= 'SE' '(' <CONDICAO> ')' <CMDS> 'SENAO' <CMDS> 'FIM_SE' 
<CMD_ENQUANTO> ::= 'ENQUANTO' <CONDICAO> <CMDS> 'FIM_ENQUANTO'
<CMD_PARA> ::= 'PARA' <VARIAVEL> '<-' <E> 'ATE' <E> <CMDS> 'FIM_PARA' 
<CMD_ATRIBUICAO> ::= <VARIAVEL> '<-' <E>
<VARIAVEL>  ::= <ID>
<CMD_LER> ::= 'LER' '(' <VARIAVEL> ')' 
<CMD_ESCREVER> ::= 'ESCREVER' '(' <E> ')'
<CMD_FATORIAL> ::= 'FATORIAL' <VARIAVEL> '<-' <E> '!' 
<CMD_FACA> ::= 'FACA' <E> 'VEZES' <CMDS> 'FIM_FACA'
<CMD_REPITA> ::= 'REPITA' <CMDS> 'ATE' <CONDICAO>
<CMD_CASO> ::= 'CASO' <CASOS> 'FIM_CASO'
<CASOS> ::= <CASO> ';' <CASOS>
<CASOS> ::= <CASO>
<CASO> ::= 'QUANDO' <CONDICAO> 'FACA' <CMDS>
<CONDICAO> ::= <E> '>' <E> 
<CONDICAO> ::= <E> '>=' <E> 
<CONDICAO> ::= <E> '<>' <E> 
<CONDICAO> ::= <E> '<=' <E> 
<CONDICAO> ::= <E> '<' <E> 
<CONDICAO> ::= <E> '==' <E>
<E> ::= <E> + <T>
<E> ::= <E> - <T>
<E> ::= <T>
<T> ::= <T> * <F>
<T> ::= <T> @ <F>
<T> ::= <T> / <F>
<T> ::= <T> % <F>
<T> ::= <F>
<F> ::= -<X>
<F> ::= <X> ** <F>
<F> ::= <X>
<X> ::= '(' <E> ')'
<X> ::= [0-9]+('.'[0-9]+)
<X> ::= <VAR>
<ID> ::= [A-Z]+([A-Z]_[0-9]*)

*/

public class Semantico {

	  // Lista de tokens	
  static final int T_PROGRAMA        =   1;
  static final int T_FIM             =   2;
  static final int T_VARIAVEIS       =   3;
  static final int T_VIRGULA         =   4;
  static final int T_PONTO_VIRGULA   =   5;
  static final int T_SE              =   6;
  static final int T_SENAO           =   7;
  static final int T_FIM_SE          =   8;
  static final int T_ENQUANTO        =   9;
  static final int T_FIM_ENQUANTO    =  10;
  static final int T_PARA            =  11;
  static final int T_SETA            =  12;
  static final int T_ATE             =  13;
  static final int T_FIM_PARA        =  14;
  static final int T_LER             =  15;
  static final int T_ABRE_PAR        =  16;
  static final int T_FECHA_PAR       =  17;
  static final int T_ESCREVER        =  18;
  static final int T_MAIOR           =  19;
  static final int T_MENOR           =  20;
  static final int T_MAIOR_IGUAL     =  21;
  static final int T_MENOR_IGUAL     =  22;
  static final int T_IGUAL           =  23;
  static final int T_DIFERENTE       =  24;
  static final int T_MAIS            =  25;
  static final int T_MENOS           =  26;
  static final int T_VEZES           =  27;
  static final int T_DIVIDIDO        =  28;
  static final int T_RESTO           =  29;
  static final int T_ELEVADO         =  30;
  static final int T_NUMERO          =  31;
  static final int T_ID              =  32;
  static final int T_FACA            =  33;
  static final int T_VEZES_FACA      =  34;
  static final int T_FIM_FACA        =  35;
  static final int T_REPITA          =  36;
  static final int T_CASO            =  37;
  static final int T_QUANDO          =  38;
  static final int T_FIM_CASO        =  39;
  static final int T_PONTO           =  40;
  static final int T_FATORIAL        =  41;
  static final int T_EXCLAMACAO      =  42;
  static final int T_SOMA_DOBRA      =  43;
  
  static final int T_FIM_FONTE       =  90;
  static final int T_ERRO_LEX        =  98;
  static final int T_NULO            =  99;

  static final int FIM_ARQUIVO       = 226;

  static final int E_SEM_ERROS       =   0;
  static final int E_ERRO_LEXICO     =   1;
  static final int E_ERRO_SINTATICO  =   2;
  static final int E_ERRO_SEMANTICO  =   3;

  // Variaveis que surgem no Lexico
  static File arqFonte;
  static BufferedReader rdFonte;
  static File arqDestino;
  static char   lookAhead;
  static int    token;
  static String lexema;
  static int    ponteiro;
  static String linhaFonte;
  static int    linhaAtual;
  static int    colunaAtual;
  static String mensagemDeErro;
  static StringBuffer tokensIdentificados = new StringBuffer();

  // Variaveis adicionadas para o sintatico
  static StringBuffer 	regrasReconhecidas = new StringBuffer();
  static int 			estadoCompilacao;
  
  // Variaveis adicionadas para o semantico
  static String 		    ultimoLexema; // criada para poder usar no codigo
                                          // guardar o lexema anterior
  static StringBuffer       codigoPython = new StringBuffer();
  static int 			    nivelIdentacao = 0; // para saber quantos espaços eu dou
  static String		        exp_0;
  static String		        exp_1;
  static String	    	    exp_2;
  static String	    	    exp_alvo;
  static NodoPilhaSemantica nodo;
  static NodoPilhaSemantica nodo_0;
  static NodoPilhaSemantica nodo_1;
  static NodoPilhaSemantica nodo_2;
  static PilhaSemantica     pilhaSemantica = new PilhaSemantica();
  static HashMap<String,Integer> tabelaSimbolos = new HashMap<String,Integer>();
  
  public static void main( String s[] ) throws ErroLexicoException
  {
      try {
          abreArquivo();
          abreDestino();
          linhaAtual     = 0;
          colunaAtual    = 0;
          ponteiro       = 0;
          linhaFonte     = "";
          token          = T_NULO;
          mensagemDeErro = "";
          tokensIdentificados.append( "Tokens reconhecidos: \n\n" );
          regrasReconhecidas.append( "\n\nRegras reconhecidas: \n\n" );
          estadoCompilacao 	= E_SEM_ERROS;

          // posiciono no primeiro token
          movelookAhead();
          buscaProximoToken();

          analiseSintatica();

          exibeSaida();

          gravaSaida( arqDestino );

          fechaFonte();

      } catch( FileNotFoundException fnfe ) {
          JOptionPane.showMessageDialog( null, "Arquivo nao existe!", "FileNotFoundException!", JOptionPane.ERROR_MESSAGE );
      } catch( UnsupportedEncodingException uee ) {
          JOptionPane.showMessageDialog( null, "Erro desconhecido", "UnsupportedEncodingException!", JOptionPane.ERROR_MESSAGE );
      } catch( IOException ioe ) {
          JOptionPane.showMessageDialog( null, "Erro de io: " + ioe.getMessage(), "IOException!", JOptionPane.ERROR_MESSAGE );
      } catch( ErroLexicoException ele ) {
          JOptionPane.showMessageDialog( null, ele.getMessage(), "Erro Lexico Exception!", JOptionPane.ERROR_MESSAGE );
      } catch( ErroSintaticoException ese ) {
          JOptionPane.showMessageDialog( null, ese.getMessage(), "Erro Sintatico Exception!", JOptionPane.ERROR_MESSAGE );
      } catch( ErroSemanticoException esme ) {
          JOptionPane.showMessageDialog( null, esme.getMessage(), "Erro Semantico Exception!", JOptionPane.ERROR_MESSAGE );          
      } finally {
          System.out.println( "Execucao terminada!" );
      }
  }

  static void analiseSintatica() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {

      g();

      if ( estadoCompilacao == E_ERRO_LEXICO ) {
          JOptionPane.showMessageDialog( null, mensagemDeErro, "Erro Lexico!", JOptionPane.ERROR_MESSAGE );
      } else if ( estadoCompilacao == E_ERRO_SINTATICO ) {
          JOptionPane.showMessageDialog( null, mensagemDeErro, "Erro Sintatico!", JOptionPane.ERROR_MESSAGE );
      } else if ( estadoCompilacao == E_ERRO_SEMANTICO ) {
          JOptionPane.showMessageDialog( null, mensagemDeErro, "Erro Sintatico!", JOptionPane.ERROR_MESSAGE );
      } else {
          JOptionPane.showMessageDialog( null, "Analise Sintatica terminada sem erros", "Analise Sintatica terminada!", JOptionPane.INFORMATION_MESSAGE );
		  acumulaRegraSintaticaReconhecida( "<G>" );
      }
  }
  
  // <G> ::= 'PROGRAMA' <LISTA> <CMDS> 'FIM'
  private static void g() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_PROGRAMA ) {
		  buscaProximoToken();
		  regraSemantica( 0 );  
		  lista();
		  cmds();
		  if ( token == T_FIM ) {
			  buscaProximoToken();
			  regraSemantica( 1 );
			  acumulaRegraSintaticaReconhecida( "<G> ::= 'PROGRAMA' <LISTA> <CMDS> 'FIM'" );
		  } else {
			  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\n('fim') esperado, mas encontrei: " + lexema );
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\n('programa') esperado, mas encontrei: " + lexema );
	  }
  }

  // <LISTA> ::= 'VARIAVEIS' <VARS> ';'
  private static void lista() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_VARIAVEIS ) {
		  buscaProximoToken();
		  vars();
		  if ( token == T_PONTO_VIRGULA ) {
			  buscaProximoToken();
			  acumulaRegraSintaticaReconhecida( "<LISTA> ::= 'VARIAVEIS' <VARS> ';'" );
		  } else {
			  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\n';' esperado, mas encontrei: " + lexema );
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\n('variaveis') esperado, mas encontrei: " + lexema );
	  }
  }
  
  // <VARS> ::= <VAR> , <VARS> | <VAR> 
  private static void vars() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  var();
	  while ( token == T_VIRGULA ) {
		  buscaProximoToken();
		  var();
	  }
	  acumulaRegraSintaticaReconhecida( "<VARS> ::= <VAR> , <VARS> | <VAR>" );
  }
  
  // <VAR> ::= <ID> 
  private static void var() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
      id();
      regraSemantica( 2 );
	  acumulaRegraSintaticaReconhecida( "<VAR> ::= <ID>" );
  }

  // <VARIAVEL> ::= <ID> 
  private static void variavel() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
      id();
      regraSemantica( 4 );
	  acumulaRegraSintaticaReconhecida( "<VARIAVEL> ::= <ID>" );
  }

  // <ID> ::= [A-Z]+([A-Z]_[0-9])*
  private static void id() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	if ( token == T_ID ) {
		buscaProximoToken();
		acumulaRegraSintaticaReconhecida( "<ID> ::= [A-Z]+([A-Z]_[0-9])*" );
	} else {
		registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nEsperava um identificador. Encontrei: " + lexema );
	}
  }
   
  // <CMDS> ::= <CMD> ; <CMDS> | <CMD>
  private static void cmds() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  cmd();
	  while ( token == T_PONTO_VIRGULA ) {
		  buscaProximoToken();
		  cmd();
	  } 
	  acumulaRegraSintaticaReconhecida( "<CMDS> ::= <CMD> ; <CMDS> | <CMD>" );
  }
  
  // <CMD> ::= <CMD_SE>
  // <CMD> ::= <CMD_ENQUANTO>
  // <CMD> ::= <CMD_PARA>
  // <CMD> ::= <CMD_ATRIBUICAO>
  // <CMD> ::= <CMD_LER>
  // <CMD> ::= <CMD_ESCREVER>
  private static void cmd() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
      switch ( token ) {
      case T_SE: cmd_se(); break;
      case T_ENQUANTO: cmd_enquanto(); break;
      case T_PARA: cmd_para(); break;
      case T_ID: cmd_atribuicao(); break;
      case T_LER: cmd_ler(); break;
      case T_ESCREVER: cmd_escrever(); break;
      case T_FACA: cmd_faca(); break;
      case T_REPITA: cmd_repita(); break;
      case T_CASO: cmd_caso(); break;
      case T_FATORIAL: cmd_fatorial(); break;
      default:
          registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\nComando nao identificado va aprender a programar pois encontrei: " + lexema );
      }
	  acumulaRegraSintaticaReconhecida( "<CMD> ::= <CMD_SE>|<CMD_ENQUANTO>|<CMD_PARA>|<CMD_ATRIBUICAO>|<CMD_LER>|<CMD_ESCREVER>" );
  }
  
  // <CMD_SE> ::= 'SE' '(' <CONDICAO> ')' <CMDS> 'FIM_SE' 
  // <CMD_SE> ::= 'SE' '(' <CONDICAO> ')' <CMDS> 'SENAO' <CMDS> 'FIM_SE' 
  private static void cmd_se() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_SE) {
		  buscaProximoToken();
		  if ( token == T_ABRE_PAR ) {
			  buscaProximoToken();
			  condicao();
			  regraSemantica( 17 );
			  if ( token == T_FECHA_PAR ) {
				  buscaProximoToken();
                  cmds();
                  regraSemantica( 16 );
				  if ( token == T_SENAO ) {
					  buscaProximoToken();
					  regraSemantica( 18 );
					  cmds();
					  regraSemantica( 16 );
				  }
				  if ( token == T_FIM_SE ) {
					  buscaProximoToken();
					  acumulaRegraSintaticaReconhecida( "<CMD_SE> ::= 'SE' <CONDICAO> <CMDS> ( 'FIM_SE'|'SENAO' <CMDS> 'FIM_SE' )" );
				  } else {
					  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'fim_se' esperado mas encontrei: " + lexema );  
				  }
			  } else {
				  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n')' esperado mas encontrei: " + lexema );
			  }
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'(' esperado mas encontrei: " + lexema ); 
		  }
	  }
  }
  
  // <CMD_ENQUANTO> ::= 'ENQUANTO' <CONDICAO> <CMDS> 'FIM_ENQUANTO'
  private static void cmd_enquanto() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_ENQUANTO ) {
		  buscaProximoToken();
		  condicao();
		  regraSemantica( 15 );
		  cmds();
		  regraSemantica( 16 );
		  if ( token == T_FIM_ENQUANTO ) {
			  buscaProximoToken();
			  acumulaRegraSintaticaReconhecida( "<CMD_ENQUANTO> ::= 'ENQUANTO' <CONDICAO> <CMDS> 'FIM_ENQUANTO'" );
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'fim enquanto' esperado mas encontrei: " + lexema );
		  }
	  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'enquanto' esperado mas encontrei: " + lexema ); 
	  }	  
  }

  // <CMD_PARA> ::= 'PARA' <VAR> '<-' <E> 'ATE' <E> <CMDS> 'FIM_PARA' 
  private static void cmd_para() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_PARA ) {
		  buscaProximoToken();
		  variavel();
		  if ( token == T_SETA ) {
			  buscaProximoToken();
			  e();
			  if ( token == T_ATE ) {
				  buscaProximoToken();
				  e();
				  regraSemantica( 30 );
				  cmds();
				  if ( token == T_FIM_PARA ) {
					  buscaProximoToken();
					  regraSemantica( 16 );
					  acumulaRegraSintaticaReconhecida( "<CMD_PARA> ::= 'PARA' <VAR> '<-' <E> 'ATE' <E> <CMDS> 'FIM_PARA'" );
				  } else {
					  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'fim_para' esperado mas encontrei: " + lexema );
				  }
			  } else {
				  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Ate' esperado mas encontrei: " + lexema );
			  }
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'<-' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Para' esperado mas encontrei: " + lexema );
	  }
  }  
  
  // <CMD_ATRIBUICAO> ::= <VAR> '<-' <E>
  private static void cmd_atribuicao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  variavel();
	  if ( token == T_SETA ) {
		  buscaProximoToken();
		  e();
		  regraSemantica( 3 );
		  acumulaRegraSintaticaReconhecida( "<CMD_ATRIBUICAO> ::= <VAR> '<-' <E>" );
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'<-' esperado mas encontrei: " + lexema );		  
	  }
  }
  
  // <CMD_LER> ::= 'LER' '(' <VAR> ')' 
  private static void cmd_ler() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_LER ) {
		  buscaProximoToken();
		  if ( token == T_ABRE_PAR ) {
			  buscaProximoToken();
			  variavel();
			  if ( token == T_FECHA_PAR ) {
				  buscaProximoToken();
				  regraSemantica( 14 );
				  acumulaRegraSintaticaReconhecida( "<CMD_LER> ::= 'LER' '(' <VAR> ')'" );
			  } else {
				  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n')' esperado mas encontrei: " + lexema );
			  }
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'(' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Ler' esperado mas encontrei: " + lexema );
	  }
  }

  // <CMD_ESCREVER> ::= 'ESCREVER' '(' <E> ')'
  private static void cmd_escrever() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_ESCREVER ) {
		  buscaProximoToken();
		  if ( token == T_ABRE_PAR ) {
			  buscaProximoToken();
			  e();
			  if ( token == T_FECHA_PAR ) {
				  buscaProximoToken();
				  regraSemantica( 25 );
				  acumulaRegraSintaticaReconhecida( "<CMD_ESCREVER> ::= 'ESCREVER' '(' <E> ')'" );
			  } else {
				  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n')' esperado mas encontrei: " + lexema );
			  }
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'(' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Escrever' esperado mas encontrei: " + lexema );
	  }
  }
  
  // <CMD_FACA> ::= 'FACA' <E> 'VEZES' <CMDS> 'FIM_FACA'
  private static void cmd_faca() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_FACA ) {
		  buscaProximoToken();
		  e();
		  regraSemantica( 26 );
		  if ( token == T_VEZES_FACA ) {
			  buscaProximoToken();
			  cmds();
			  if ( token == T_FIM_FACA ) {
				  buscaProximoToken();
				  regraSemantica( 27 );
				  acumulaRegraSintaticaReconhecida( "<CMD_FACA> ::= 'FACA' <E> 'VEZES' <CMDS> 'FIM_FACA'" );
			  } else {
				  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Fim_faca' esperado mas encontrei: " + lexema );
			  }
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Vezes' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Faca' esperado mas encontrei: " + lexema );
	  }
  }

  // <CMD_REPITA> ::= 'REPITA' <CMDS> 'ATE' <CONDICAO>
  private static void cmd_repita() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_REPITA ) {
		  buscaProximoToken();
		  regraSemantica( 28 );
		  cmds();
		  if ( token == T_ATE ) {
			  buscaProximoToken();
			  condicao();
			  regraSemantica( 29 );
			  acumulaRegraSintaticaReconhecida( "<CMD_REPITA> ::= 'REPITA' <CMDS> 'ATE' <CONDICAO>" );
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Ate' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Repita' esperado mas encontrei: " + lexema );
	  }
  }


  // <CMD_FATORIAL> ::= 'FATORIAL' <VARIAVEL> '<-' <E> '!' 
  private static void cmd_fatorial() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_FATORIAL ) {
		  buscaProximoToken();
          variavel();
		  if ( token == T_SETA ) {
			  buscaProximoToken();
			  e();
			  if ( token == T_EXCLAMACAO ) {
				  buscaProximoToken();
				  regraSemantica( 33 ); 
				  acumulaRegraSintaticaReconhecida( "<CMD_FATORIAL> ::= 'FATORIAL' <VARIAVEL> '<-' <E> '!'" );
			  } else {
				  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Ate' esperado mas encontrei: " + lexema ); 
			  }
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Ate' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Repita' esperado mas encontrei: " + lexema );
	  }
  }
  
  // <CMD_CASO> ::= 'CASO' <CASOS> 'FIM_CASO'
  private static void cmd_caso() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_CASO ) {
		  buscaProximoToken();
		  regraSemantica( 31 );
		  casos();
		  if ( token == T_FIM_CASO ) {
			  buscaProximoToken();
			  acumulaRegraSintaticaReconhecida( "<CMD_CASO> ::= 'CASO' <CASOS> 'FIM_CASO'" );
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Ate' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Repita' esperado mas encontrei: " + lexema );
	  }
  }
  
  // <CASOS> ::= <CASO> '.' <CASOS>
  // <CASOS> ::= <CASO>
  private static void casos() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  caso();
	  while ( token == T_PONTO ) {
		  buscaProximoToken();
		  caso();
	  } 
	  acumulaRegraSintaticaReconhecida( "<CMDS> ::= <CMD> ; <CMDS> | <CMD>" );
  }
  
  // <CASO> ::= 'QUANDO' <CONDICAO> 'FACA' <CMDS>
  private static void caso() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {

	  if ( token == T_QUANDO ) {
		  buscaProximoToken();
		  condicao();
		  regraSemantica( 32 );
		  if ( token == T_FACA ) {
			  buscaProximoToken();
			  cmds();
			  acumulaRegraSintaticaReconhecida( "<CASO> ::= 'QUANDO' <CONDICAO> 'FACA' <CMDS>" );
			  regraSemantica( 16 );
		  } else {
			  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Faca' esperado mas encontrei: " + lexema ); 
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n'Quando' esperado mas encontrei: " + lexema );
	  }
	  
  }

  // <CONDICAO> ::= <E> '>' <E> 
  // <CONDICAO> ::= <E> '>=' <E> 
  // <CONDICAO> ::= <E> '<>' <E> 
  // <CONDICAO> ::= <E> '<=' <E> 
  // <CONDICAO> ::= <E> '<' <E> 
  // <CONDICAO> ::= <E> '==' <E>
  private static void condicao() throws ErroLexicoException, IOException, ErroSintaticoException, ErroSemanticoException {
	  e();
	  switch ( token ) {
	  case T_MAIOR: buscaProximoToken(); e(); regraSemantica( 19 ); break; 
	  case T_MENOR: buscaProximoToken(); e(); regraSemantica( 20 ); break; 
	  case T_MAIOR_IGUAL: buscaProximoToken(); e(); regraSemantica( 21 ); break; 
	  case T_MENOR_IGUAL: buscaProximoToken(); e(); regraSemantica( 22 ); break; 
	  case T_IGUAL: buscaProximoToken(); e(); regraSemantica( 23 ); break; 
	  case T_DIFERENTE: buscaProximoToken(); e(); regraSemantica( 24 ); break;
	  default: registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nEsperava um operador logico. Encontrei: " + lexema );
	  }
	  acumulaRegraSintaticaReconhecida( "<CONDICAO> ::= <E> ('>'|'>='|'<>'|'<='|'<'|'==') <E> " );
  }
  
  // <E> ::= <E> + <T>
  // <E> ::= <E> - <T>
  // <E> ::= <T>
  private static void e() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  t();
	  while ( (token == T_MAIS) || (token == T_MENOS) ) {
		  switch( token ) {
	      case T_MAIS: { buscaProximoToken();
		  				 t();
		  				 regraSemantica( 5 );
	      	   }
	           break;
	      case T_MENOS: { buscaProximoToken();
	      				  t();
	      				  regraSemantica( 6 );
	      	   }
	           break;
		  }
	  }
	  acumulaRegraSintaticaReconhecida( "<E> ::= <E> + <T>|<E> - <T>|<T> " );
  }
  
  // <T> ::= <T> * <F>
  // <T> ::= <T> / <F>
  // <T> ::= <T> % <F>
  // <T> ::= <F>
  private static void t() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  f();
	  while ( (token == T_VEZES) || (token == T_DIVIDIDO) || (token == T_RESTO) || (token == T_SOMA_DOBRA) ) {
		  switch ( token ) {
		  case T_VEZES: { buscaProximoToken();
		  				  f();
		  				  regraSemantica( 7 );
		  				}
		                break;
		  case T_DIVIDIDO: { buscaProximoToken();
			  				 f();
			  				 regraSemantica( 8 );
						   }
		                break;
		  case T_RESTO: { buscaProximoToken();
			 		  	  f();
			 		  	  regraSemantica( 9 );
		  				}
		                break;
		  case T_SOMA_DOBRA: { buscaProximoToken();
		  	                   f();
		  	                   regraSemantica( 34 );
			                 }
                             break;
		  }
	  }
	  acumulaRegraSintaticaReconhecida( "<T> ::= <T> * <F>|<T> / <F>|<T> % <F>|<F>" );
  }
  
  // <F> ::= -<F>
  // <F> ::= <X> ** <F>
  // <F> ::= <X>     
  private static void f() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_MENOS ) {
		  buscaProximoToken();
		  f();
	  } else {
		  x();
		  while ( token == T_ELEVADO ) {
			  buscaProximoToken();
	          x();
	          regraSemantica( 10 );
		  }
	  }
	  acumulaRegraSintaticaReconhecida( "<F> ::= -<F>|<X> ** <F>|<X> " );
	  
  }
  
  // <X> ::= '(' <E> ')'
  // <X> ::= [0-9]+('.'[0-9]+)
  // <X> ::= <VAR>
  private static void x() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  switch ( token ) {
	  case T_ID: buscaProximoToken(); acumulaRegraSintaticaReconhecida( "<X> ::= <VAR>" ); regraSemantica( 11 ); break;
	  case T_NUMERO: buscaProximoToken(); acumulaRegraSintaticaReconhecida( "<X> ::= [0-9]+('.'[0-9]+)" ); regraSemantica( 12 ); break;
	  case T_ABRE_PAR: {
	       buscaProximoToken(); 
	       e();
	       if ( token == T_FECHA_PAR ) {
	    	   buscaProximoToken();
	    	   acumulaRegraSintaticaReconhecida( "<X> ::= '(' <E> ')'" );
	       } else {
			   registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\n')' esperado mas encontrei: " + lexema );
	       }
	       regraSemantica( 13 );
	      } break;
	   default: registraErroSintatico( "Erro Sintatico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\nFator invalido: encontrei: " + lexema );   
	  }
  }
  
  static void fechaFonte() throws IOException
  {
      rdFonte.close();
  }

  static void movelookAhead() throws IOException
  {
	  
    if ( ( ponteiro + 1 ) > linhaFonte.length() ) {

    	linhaAtual++;
        ponteiro = 0;
        
        
        if ( ( linhaFonte = rdFonte.readLine() ) == null ) {
            lookAhead = FIM_ARQUIVO;
        } else {

        	StringBuffer sbLinhaFonte = new StringBuffer( linhaFonte );
        	sbLinhaFonte.append( '\13' ).append( '\10' );
        	linhaFonte = sbLinhaFonte.toString();
        	
            lookAhead = linhaFonte.charAt( ponteiro );
        }
    } else {
        lookAhead = linhaFonte.charAt( ponteiro );
    }

    // Se comentar esse if, eu terei uma linguagem 
    // que diferencia minusculas de maiusculas
    if ( ( lookAhead >= 'a' ) &&
         ( lookAhead <= 'z' ) ) {
        lookAhead = (char) ( lookAhead - 'a' + 'A' );
    }

    ponteiro++;
    colunaAtual = ponteiro + 1;
  }

  static void buscaProximoToken() throws IOException, ErroLexicoException
  {
	int i, j;
    
    if ( lexema != null ) {
        ultimoLexema = new String( lexema );
    }
    
    StringBuffer sbLexema = new StringBuffer( "" );

    // Salto espaçoes enters e tabs até o inicio do proximo token
  	while ( ( lookAhead == 9 ) ||
	        ( lookAhead == '\n' ) ||
	        ( lookAhead == 8 ) ||
	        ( lookAhead == 11 ) ||
	        ( lookAhead == 12 ) ||
	        ( lookAhead == '\r' ) ||
	        ( lookAhead == 32 ) )
    {
        movelookAhead();
    }

    /*--------------------------------------------------------------*
     * Caso o primeiro caracter seja alfabetico, procuro capturar a *
     * sequencia de caracteres que se segue a ele e classifica-la   *
     *--------------------------------------------------------------*/
    if ( ( lookAhead >= 'A' ) && ( lookAhead <= 'Z' ) ) {   
        sbLexema.append( lookAhead );
        movelookAhead();

        while ( ( ( lookAhead >= 'A' ) && ( lookAhead <= 'Z' ) ) ||
        		( ( lookAhead >= '0' ) && ( lookAhead <= '9' ) ) || ( lookAhead == '_' ) )
        {
            sbLexema.append( lookAhead );
            movelookAhead();
        }

        lexema = sbLexema.toString();  

        /* Classifico o meu token como palavra reservada ou id */
        if ( lexema.equals( "PROGRAMA" ) )
            token = T_PROGRAMA;
        else if ( lexema.equals( "FIM" ) )
            token = T_FIM;
        else if ( lexema.equals( "VARIAVEIS" ) )
            token = T_VARIAVEIS;
        else if ( lexema.equals( "SE" ) )
            token = T_SE;
        else if ( lexema.equals( "SENAO" ) )
            token = T_SENAO;
        else if ( lexema.equals( "FIM_SE" ) )
            token = T_FIM_SE;
        else if ( lexema.equals( "ENQUANTO" ) )
            token = T_ENQUANTO;
        else if ( lexema.equals( "FIM_ENQUANTO" ) )
            token = T_FIM_ENQUANTO;
        else if ( lexema.equals( "PARA" ) )
            token = T_PARA;
        else if ( lexema.equals( "ATE" ) )
            token = T_ATE;
        else if ( lexema.equals( "FIM_PARA" ) )
            token = T_FIM_PARA;
        else if ( lexema.equals( "LER" ) )
            token = T_LER;
        else if ( lexema.equals( "ESCREVER" ) )
            token = T_ESCREVER;
        else if ( lexema.equals( "FACA" ) )
            token = T_FACA;
        else if ( lexema.equals( "VEZES" ) )
            token = T_VEZES_FACA;
        else if ( lexema.equals( "FIM_FACA" ) )
            token = T_FIM_FACA;
        else if ( lexema.equals( "REPITA" ) )
            token = T_REPITA;
        else if ( lexema.equals( "CASO" ) )
            token = T_CASO;
        else if ( lexema.equals( "QUANDO" ) )
            token = T_QUANDO;
        else if ( lexema.equals( "FATORIAL" ) )
            token = T_FATORIAL;
        else if ( lexema.equals( "FIM_CASO" ) )
            token = T_FIM_CASO;
        else {
        	token = T_ID;
        }
    } else if ( ( lookAhead >= '0' ) && ( lookAhead <= '9' ) ) {
        sbLexema.append( lookAhead );
        movelookAhead();
        while ( ( lookAhead >= '0' ) && ( lookAhead <= '9' ) )
        {
            sbLexema.append( lookAhead );
            movelookAhead();
        }
        token = T_NUMERO;    	
    } else if ( lookAhead == '(' ){
        sbLexema.append( lookAhead );
        token = T_ABRE_PAR;    	
        movelookAhead();
    } else if ( lookAhead == ')' ){
        sbLexema.append( lookAhead );
        token = T_FECHA_PAR;    	
        movelookAhead();
    } else if ( lookAhead == ';' ){
        sbLexema.append( lookAhead );
        token = T_PONTO_VIRGULA;    	
        movelookAhead();
    } else if ( lookAhead == ',' ){
        sbLexema.append( lookAhead );
        token = T_VIRGULA;    	
        movelookAhead();
    } else if ( lookAhead == '!' ){
        sbLexema.append( lookAhead );
        token = T_EXCLAMACAO;    	
        movelookAhead();        
    } else if ( lookAhead == '.' ){
        sbLexema.append( lookAhead );
        token = T_PONTO;    	
        movelookAhead();
    } else if ( lookAhead == '+' ){
        sbLexema.append( lookAhead );
        token = T_MAIS;    	
        movelookAhead();
    } else if ( lookAhead == '-' ){
        sbLexema.append( lookAhead );
        token = T_MENOS;    	
        movelookAhead();
    } else if ( lookAhead == '*' ){
        sbLexema.append( lookAhead );
        movelookAhead();
        if ( lookAhead == '*' ) {
            sbLexema.append( lookAhead );
            movelookAhead();
            token = T_ELEVADO;    	
        } else {
            token = T_VEZES;    	
        }
    } else if ( lookAhead == '/' ){
        sbLexema.append( lookAhead );
        token = T_DIVIDIDO;    	
        movelookAhead();
    } else if ( lookAhead == '%' ){
        sbLexema.append( lookAhead );
        token = T_RESTO;    	
        movelookAhead();
    } else if ( lookAhead == '@' ){
        sbLexema.append( lookAhead );
        token = T_SOMA_DOBRA;    	
        movelookAhead();
    } else if ( lookAhead == '<' ){
        sbLexema.append( lookAhead );
        movelookAhead();
        if ( lookAhead == '>' ) {
            sbLexema.append( lookAhead );
            movelookAhead();
            token = T_DIFERENTE;
        } else if ( lookAhead == '-' ) {  
            sbLexema.append( lookAhead );
            movelookAhead();
            token = T_SETA;
        } else if ( lookAhead == '=' ) {  
            sbLexema.append( lookAhead );
            movelookAhead();
            token = T_MENOR_IGUAL;
        } else {
            token = T_MENOR;    	
        }
    } else if ( lookAhead == '>' ){
        sbLexema.append( lookAhead );
        movelookAhead();
        if ( lookAhead == '=' ) {
            sbLexema.append( lookAhead );
            movelookAhead();
            token = T_MAIOR_IGUAL;
        } else {
            token = T_MAIOR;    	
        }        
    } else if ( lookAhead == FIM_ARQUIVO ){
         token = T_FIM_FONTE;    	
    } else {
    	token = T_ERRO_LEX;
    	sbLexema.append( lookAhead );
    }
        
    lexema = sbLexema.toString();  
    
    mostraToken();
    
    if ( token == T_ERRO_LEX ) {
    	mensagemDeErro = "Erro Léxico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\nToken desconhecido: " + lexema;
    	throw new ErroLexicoException( mensagemDeErro );
    } 
  }
  
  static void mostraToken()
  {

    StringBuffer tokenLexema = new StringBuffer( "" );
    
    switch ( token ) {
    case T_PROGRAMA    : tokenLexema.append( "T_PROGRAMA" ); break;
    case T_FIM    : tokenLexema.append( "T_FIM" ); break;
    case T_VARIAVEIS    : tokenLexema.append( "T_VARIAVEIS" ); break;
    case T_VIRGULA    : tokenLexema.append( "T_VIRGULA" ); break;
    case T_PONTO_VIRGULA    : tokenLexema.append( "T_PONTO_VIRGULA" ); break;
    case T_SE    : tokenLexema.append( "T_SE" ); break;
    case T_SENAO    : tokenLexema.append( "T_SENAO" ); break;
    case T_FIM_SE    : tokenLexema.append( "T_FIM_SE" ); break;
    case T_ENQUANTO    : tokenLexema.append( "T_ENQUANTO" ); break;
    case T_FIM_ENQUANTO    : tokenLexema.append( "T_FIM_ENQUANTO" ); break;
    case T_PARA            : tokenLexema.append( "T_PARA" ); break;
    case T_SETA            : tokenLexema.append( "T_SETA" ); break;
    case T_ATE             : tokenLexema.append( "T_ATE" ); break;
    case T_FIM_PARA        : tokenLexema.append( "T_FIM_PARA" ); break;
    case T_LER             : tokenLexema.append( "T_LER" ); break;
    case T_ABRE_PAR        : tokenLexema.append( "T_ABRE_PAR" ); break;
    case T_FECHA_PAR       : tokenLexema.append( "T_FECHA_PAR" ); break;
    case T_ESCREVER        : tokenLexema.append( "T_ESCREVER" ); break;
    case T_MAIOR           : tokenLexema.append( "T_MAIOR" ); break;
    case T_MENOR           : tokenLexema.append( "T_MENOR" ); break;
    case T_MAIOR_IGUAL     : tokenLexema.append( "T_MAIOR_IGUAL" ); break;
    case T_MENOR_IGUAL     : tokenLexema.append( "T_MENOR_IGUAL" ); break;
    case T_IGUAL           : tokenLexema.append( "T_IGUAL" ); break;
    case T_DIFERENTE       : tokenLexema.append( "T_DIFERENTE" ); break;
    case T_MAIS            : tokenLexema.append( "T_MAIS" ); break;
    case T_MENOS           : tokenLexema.append( "T_MENOS" ); break;
    case T_VEZES           : tokenLexema.append( "T_VEZES" ); break;
    case T_DIVIDIDO        : tokenLexema.append( "T_DIVIDIDO" ); break;
    case T_RESTO           : tokenLexema.append( "T_RESTO" ); break;
    case T_ELEVADO         : tokenLexema.append( "T_ELEVADO" ); break;
    case T_NUMERO          : tokenLexema.append( "T_NUMERO" ); break;
    case T_ID              : tokenLexema.append( "T_ID" ); break;
    case T_FIM_FONTE       : tokenLexema.append( "T_FIM_FONTE" ); break;
    case T_ERRO_LEX        : tokenLexema.append( "T_ERRO_LEX" ); break;
    case T_NULO            : tokenLexema.append( "T_NULO" ); break;
    case T_FACA            : tokenLexema.append( "T_FACA" ); break;
    case T_VEZES_FACA      : tokenLexema.append( "T_VEZES_FACA" ); break;
    case T_FIM_FACA        : tokenLexema.append( "T_FIM_FACA" ); break;
    case T_REPITA          : tokenLexema.append( "T_REPITA" ); break;
    case T_CASO            : tokenLexema.append( "T_CASO" ); break;
    case T_QUANDO          : tokenLexema.append( "T_QUANDO" ); break;
    case T_FIM_CASO        : tokenLexema.append( "T_FIM_CASO" ); break;
    case T_PONTO           : tokenLexema.append( "T_PONTO" ); break;
    case T_FATORIAL        : tokenLexema.append( "T_FATORIAL" ); break;
    case T_EXCLAMACAO      : tokenLexema.append( "T_EXCLAMACAO" ); break;
    case T_SOMA_DOBRA      : tokenLexema.append( "T_SOMA_DOBRA" ); break;
    
    default                : tokenLexema.append( "N/A" ); break;
    }
    System.out.println( tokenLexema.toString() + " ( " + lexema + " )" );
    acumulaToken( tokenLexema.toString() + " ( " + lexema + " )" );
    tokenLexema.append( lexema );
  }
  
  private static void abreArquivo() {

		JFileChooser fileChooser = new JFileChooser();
		
		fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

		FiltroSab filtro = new FiltroSab();
	    
		fileChooser.addChoosableFileFilter( filtro );
		int result = fileChooser.showOpenDialog( null );
		
		if( result == JFileChooser.CANCEL_OPTION ) {
			return;
		}
		
		arqFonte = fileChooser.getSelectedFile();
		abreFonte( arqFonte ); 	

	}


	private static boolean abreFonte( File fileName ) {

		if( arqFonte == null || fileName.getName().trim().equals( "" ) ) {
			JOptionPane.showMessageDialog( null, "Nome de Arquivo Invalido", "Nome de Arquivo Invalido", JOptionPane.ERROR_MESSAGE );
			return false;
		} else {
			linhaAtual = 1;
	        try {
				FileReader fr = new FileReader( arqFonte );
				rdFonte = new BufferedReader( fr );
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
			return true;
		}
	}

	private static void abreDestino() {

		JFileChooser fileChooser = new JFileChooser( "C:\\temp" );
			
		fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

		FiltroSab filtro = new FiltroSab();
		    
		fileChooser.addChoosableFileFilter( filtro );
		int result = fileChooser.showSaveDialog( null );
			
		if( result == JFileChooser.CANCEL_OPTION ) {
			return;
		}
			
		arqDestino = fileChooser.getSelectedFile();
	}	
	

	private static boolean gravaSaida( File fileName ) {

		if( arqDestino == null || fileName.getName().trim().equals( "" ) ) {
			JOptionPane.showMessageDialog( null, "Nome de Arquivo Invalido", "Nome de Arquivo Invalido", JOptionPane.ERROR_MESSAGE );
			return false;
		} else {
			FileWriter fw;
			try {
				fw = new FileWriter( arqDestino );
				BufferedWriter bfw = new BufferedWriter( fw ); 
                bfw.write( codigoPython.toString() );
                bfw.close();
				JOptionPane.showMessageDialog( null, "Arquivo Salvo: " + arqDestino, "Salvando Arquivo", JOptionPane.INFORMATION_MESSAGE );
			} catch (IOException e) {
				JOptionPane.showMessageDialog( null, e.getMessage(), "Erro de Entrada/Saida", JOptionPane.ERROR_MESSAGE );
			} 
			return true;
		}
	}
	
	public static void exibeTokens() {
		
		JTextArea texto = new JTextArea();
		texto.append( tokensIdentificados.toString() );
		JOptionPane.showMessageDialog(null, texto, "Tokens Identificados (token/lexema)", JOptionPane.INFORMATION_MESSAGE );
	}
	
	
	public static void acumulaRegraSintaticaReconhecida( String regra ) {

		regrasReconhecidas.append( regra );
		regrasReconhecidas.append( "\n" );
		
	}
	
	public static void acumulaToken( String tokenIdentificado ) {

		tokensIdentificados.append( tokenIdentificado );
		tokensIdentificados.append( "\n" );
		
	}
	
    public static void exibeSaida() {

        JTextArea texto = new JTextArea();
        texto.append( tokensIdentificados.toString() );
        JOptionPane.showMessageDialog(null, texto, "Analise Lexica", JOptionPane.INFORMATION_MESSAGE );

        texto.setText( regrasReconhecidas.toString() );
        texto.append( "\n\nStatus da Compilacao:\n\n" );
        texto.append( mensagemDeErro );

        JOptionPane.showMessageDialog(null, texto, "Resumo da Compilacao", JOptionPane.INFORMATION_MESSAGE );
    }
    
    static void registraErroSintatico( String msg ) throws ErroSintaticoException {
        if ( estadoCompilacao == E_SEM_ERROS ) {
            estadoCompilacao = E_ERRO_SINTATICO;
            mensagemDeErro = msg;
        }
        throw new ErroSintaticoException( msg ); 
    }
		
    
    static void registraErroSemantico( String msg ) {

        if ( estadoCompilacao == E_SEM_ERROS ) {
            estadoCompilacao = E_ERRO_SEMANTICO;
            mensagemDeErro = msg;
        }
    }

    static void regraSemantica( int numeroRegra ) throws ErroSemanticoException {
        System.out.println( "Regra Semantica " + numeroRegra );
        switch ( numeroRegra ) {
            case  0: 	codigoPython.append( "\nimport os\nimport sys\nimport glob\nimport string\n\n\n" );
                        codigoPython.append( "def fatorial( n ):\n" );
                		nivelIdentacao++;
                		codigoPython.append( tabulacao( nivelIdentacao ) );
                		codigoPython.append( "f = 1\n");
                		codigoPython.append( tabulacao( nivelIdentacao ) );
                		codigoPython.append( "for i in range( 1, n+1 ):\n");
                		nivelIdentacao++;
                		codigoPython.append( tabulacao( nivelIdentacao ) );
                		codigoPython.append( "f = f * i\n");
                		nivelIdentacao--;
                		codigoPython.append( tabulacao( nivelIdentacao ) );
                		codigoPython.append( "return f\n\n");
                		nivelIdentacao--;
                        codigoPython.append( "def main( ):\n" );
                		nivelIdentacao++;
                		codigoPython.append( tabulacao( nivelIdentacao ) );
                		codigoPython.append( "# Feevale compiler\n\n\n");
                		break;
            case  1: 	codigoPython.append( tabulacao( nivelIdentacao ) );
                		codigoPython.append( "pass\n\n" );
                		codigoPython.append( "if __name__ == '__main__':\n" );
                		codigoPython.append( tabulacao( nivelIdentacao ) );
                		codigoPython.append( "main( )\n" );
                		break;
            case  2:	insereNaTabelaSimbolos( ultimoLexema );
						break;
            case  3:	nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();	
						System.out.println( "Codigo 1 " + nodo_1.getCodigo() );
						System.out.println( "Codigo 2 " + nodo_2.getCodigo() );
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( nodo_1.getCodigoMinusculo() + " = " + nodo_2.getCodigoMinusculo() + "\n" );
						break;
            case  4:	if ( VeSeExisteNaTabelaSimbolos( ultimoLexema ) ) {
				            pilhaSemantica.push( ultimoLexema, 4 );
			            }
			            break;
            case  5:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + "+" + nodo_2.getCodigoMinusculo(), 5 );
						break;
            case  6:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + "-" + nodo_2.getCodigoMinusculo(), 6 );
						break;
            case  7:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + "*" + nodo_2.getCodigoMinusculo(), 7 );
						break;
            case  8:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + "/" + nodo_2.getCodigoMinusculo(), 8 );
						break;
            case  9:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + "%" + nodo_2.getCodigoMinusculo(), 9 );
						break;
            case 10:    nodo_2 = pilhaSemantica.pop();
            			nodo_1 = pilhaSemantica.pop();
            			pilhaSemantica.push( nodo_1.getCodigoMinusculo() + "**" + nodo_2.getCodigoMinusculo(), 10 );
            			break;
            case 11:	if ( VeSeExisteNaTabelaSimbolos( ultimoLexema ) ) {
	            			pilhaSemantica.push( ultimoLexema, 11 );
            			}
            			break;
            case 12:	pilhaSemantica.push( ultimoLexema, 12 );
            			break;
            case 13:	nodo_1 = pilhaSemantica.pop();
            			pilhaSemantica.push( "(" + nodo_1.getCodigoMinusculo() + ")" , 13 );            
            			break;
            case 14:    nodo_1 = pilhaSemantica.pop();
    					codigoPython.append( tabulacao( nivelIdentacao ) );
    					codigoPython.append( nodo_1.getCodigoMinusculo() + " = int( input('Informe a variavel " + nodo_1.getCodigoMinusculo() + " ') )\n" );
    					break;
            case 15:    nodo_1 = pilhaSemantica.pop();
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "while ( " + nodo_1.getCodigoMinusculo() + " ):\n" );
						nivelIdentacao++;
						break;
            case 16:    nivelIdentacao--;
						break;
            case 17:    nodo_1 = pilhaSemantica.pop();
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "if " + nodo_1.getCodigoMinusculo() + ":\n" );
						nivelIdentacao++;
						break;
            case 18:    codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "else:\n" );
						nivelIdentacao++;
						break;
            case 19:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + " > " + nodo_2.getCodigoMinusculo(), 19 );
						break;						
            case 20:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + " < " + nodo_2.getCodigoMinusculo(), 20 );
						break;						
            case 21:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + " >= " + nodo_2.getCodigoMinusculo(), 21 );
						break;						
            case 22:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + " <= " + nodo_2.getCodigoMinusculo(), 22 );
						break;						
            case 23:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + " == " + nodo_2.getCodigoMinusculo(), 23 );
						break;						
            case 24:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( nodo_1.getCodigoMinusculo() + " != " + nodo_2.getCodigoMinusculo(), 24 );
						break;		
            case 25:    nodo_1 = pilhaSemantica.pop();
    					codigoPython.append( tabulacao( nivelIdentacao ) );
    					codigoPython.append( "print ( " + nodo_1.getCodigoMinusculo() + " )\n" );
    					break;
            case 26:    nodo_1 = pilhaSemantica.pop();
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "for _i" + (nivelIdentacao+"") + " in range ( " + nodo_1.getCodigoMinusculo() + " ):\n" );
						nivelIdentacao++;
						break;
            case 27:    nivelIdentacao--;
						break;    					
            case 28:    codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "while ( true ):\n" );
						nivelIdentacao++;
						break;
            case 29:    nodo_1 = pilhaSemantica.pop();
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "if ( " + nodo_1.getCodigoMinusculo() + " ):\n" );
						nivelIdentacao++;
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "break;\n" );
            	        nivelIdentacao--;
            	        nivelIdentacao--;
						break;   
            case 30:    nodo_2 = pilhaSemantica.pop(); // exp2
                        nodo_1 = pilhaSemantica.pop(); // exp1
			            nodo_0 = pilhaSemantica.pop(); // variavel
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "for " + nodo_0.getCodigoMinusculo() + " in range( " + nodo_1.getCodigoMinusculo() + ", " + nodo_2.getCodigoMinusculo() + "+1):\n" );
						nivelIdentacao++;
			            break;						
            case 31:    codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "if (false): \n" );
						nivelIdentacao++;
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "pass \n" );
						nivelIdentacao--;
						break;
            case 32:    nodo_1 = pilhaSemantica.pop();
						codigoPython.append( tabulacao( nivelIdentacao ) );
						codigoPython.append( "elif ( " + nodo_1.getCodigoMinusculo() + " ):\n" );
						nivelIdentacao++;
						break;   
            case 33:    nodo_1 = pilhaSemantica.pop(); // exp1
                        nodo_0 = pilhaSemantica.pop(); // variavel
			            codigoPython.append( tabulacao( nivelIdentacao ) );
			            codigoPython.append( nodo_0.getCodigoMinusculo() + " = fatorial( " + nodo_1.getCodigoMinusculo() + " )\n" );
                        break;	
            case 34:    nodo_2 = pilhaSemantica.pop();
						nodo_1 = pilhaSemantica.pop();
						pilhaSemantica.push( "2 * ( " + nodo_1.getCodigoMinusculo() + "+" + nodo_2.getCodigoMinusculo() + ") ", 34 );
						break;
        }
    }
    
    private static int buscaTipoNaTabelaSimbolos(String ultimoLexema ) throws ErroSemanticoException {
    	return tabelaSimbolos.get( ultimoLexema );
	}
    
    private static boolean VeSeExisteNaTabelaSimbolos(String ultimoLexema ) throws ErroSemanticoException {
    	if ( !tabelaSimbolos.containsKey( ultimoLexema ) ) {
	    	throw new ErroSemanticoException( "Variavel " + ultimoLexema + " nao esta declarada! linha: " + linhaAtual );
    	} else {
    		return true;
    	}
	}

	private static void insereNaTabelaSimbolos(String ultimoLexema) throws ErroSemanticoException {
		if ( tabelaSimbolos.containsKey( ultimoLexema ) ) {
	    	throw new ErroSemanticoException( "Variavel " + ultimoLexema + " ja declarada! linha: " + linhaAtual );
		} else {
			tabelaSimbolos.put( ultimoLexema, 0 );
		}
	}
    
	static String tabulacao( int qtd ) {
        StringBuffer sb = new StringBuffer();
        for ( int i=0; i<qtd; i++ ) {
            sb.append( "    " );
        }
        return sb.toString();
    }
}

/**
 * Classe Interna para criacao de filtro de selecao
 */
class FiltroSab extends FileFilter {

	public boolean accept(File arg0) {
	   	 if(arg0 != null) {
	         if(arg0.isDirectory()) {
	       	  return true;
	         }
	         if( getExtensao(arg0) != null) {
	        	 if ( getExtensao(arg0).equalsIgnoreCase( "grm" ) ) {
		        	 return true;
	        	 }
	         };
	   	 }
	     return false;
	}

	/**
	 * Retorna quais extensoes poderao ser escolhidas
	 */
	public String getDescription() {
		return "*.grm";
	}
	
	/**
	 * Retorna a parte com a extensao de um arquivo
	 */
	public String getExtensao(File arq) {
	if(arq != null) {
		String filename = arq.getName();
	    int i = filename.lastIndexOf('.');
	    if(i>0 && i<filename.length()-1) {
	    	return filename.substring(i+1).toLowerCase();
	    };
	}
		return null;
	}
}
