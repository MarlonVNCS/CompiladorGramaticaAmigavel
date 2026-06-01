import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

/**
 * Semantico - Versao ajustada para a gramatica amigavel.
 * @author Ricardo Ferreira de Oliveira
 * @author Marlon Vinicius Gonçalves
 * @author Júlia Nathalie Schmitz
 */
public class Semantico {

  // Lista de tokens
  static final int T_INICIO          =   1;
  static final int T_ACABOU          =   2;
  static final int T_NUM             =   3;
  static final int T_PALAVRA         =   4;
  static final int T_FAZ             =   5;
  static final int T_RETORNA         =   6;
  static final int T_ESCREVER        =   7;
  static final int T_SERA_QUE        =   8;
  static final int T_E_SE            =   9;
  static final int T_SE_NAO          =  10;
  static final int T_CICLO           =  11;
  static final int T_VEM             =  12;
  static final int T_DE              =  13;
  static final int T_ATE             =  14;
  static final int T_MAIOR           =  15;
  static final int T_MENOR           =  16;
  static final int T_IGUAL_PALAVRA   =  17;
  static final int T_DOIS_PONTOS     =  18;
  static final int T_ABRE_PAR        =  19;
  static final int T_FECHA_PAR       =  20;
  static final int T_ABRE_CHAVE      =  21;
  static final int T_FECHA_CHAVE     =  22;
  static final int T_VIRGULA         =  23;
  static final int T_ATRIBUICAO      =  24;
  static final int T_MAIS            =  25;
  static final int T_MENOS           =  26;
  static final int T_VEZES           =  27;
  static final int T_DIVIDIDO        =  28;
  static final int T_NUMERO          =  29;
  static final int T_STRING          =  30;
  static final int T_ID              =  31;

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
  static StringBuffer  regrasReconhecidas = new StringBuffer();
  static int           estadoCompilacao;

  // Variaveis adicionadas para o semantico
  static StringBuffer codigoPython = new StringBuffer();
  static int nivelIdentacao = 0;
  static int profundidadeFuncao = 0;
  static Stack<HashMap<String, String>> escopos = new Stack<HashMap<String, String>>();
  static HashMap<String, String> funcoes = new HashMap<String, String>();

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
		  tokensIdentificados.append( "Tokens reconhecidos:\n\n" );
		  regrasReconhecidas.append( "\n\nRegras reconhecidas:\n\n" );
		  estadoCompilacao = E_SEM_ERROS;

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
	  programa();

	  if ( estadoCompilacao == E_ERRO_LEXICO ) {
		  JOptionPane.showMessageDialog( null, mensagemDeErro, "Erro Lexico!", JOptionPane.ERROR_MESSAGE );
	  } else if ( estadoCompilacao == E_ERRO_SINTATICO ) {
		  JOptionPane.showMessageDialog( null, mensagemDeErro, "Erro Sintatico!", JOptionPane.ERROR_MESSAGE );
	  } else if ( estadoCompilacao == E_ERRO_SEMANTICO ) {
		  JOptionPane.showMessageDialog( null, mensagemDeErro, "Erro Semantico!", JOptionPane.ERROR_MESSAGE );
	  } else {
		  JOptionPane.showMessageDialog( null, "Analise Sintatica terminada sem erros", "Analise Sintatica terminada!", JOptionPane.INFORMATION_MESSAGE );
		  acumulaRegraSintaticaReconhecida( "<programa>" );
	  }
  }

  // programa := 'Inicio:' bloco ':Acabou'
  private static void programa() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( token == T_INICIO ) {
		  buscaProximoToken();
		  espera( T_DOIS_PONTOS, "':'" );
		  iniciaPrograma();
		  blocoComPassoSeVazio();
		  espera( T_DOIS_PONTOS, "':'" );
		  if ( token == T_ACABOU ) {
			  buscaProximoToken();
			  finalizaPrograma();
			  acumulaRegraSintaticaReconhecida( "programa := 'Inicio:' bloco ':Acabou'" );
		  } else {
			  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\n':Acabou' esperado, mas encontrei: " + lexema );
		  }
	  } else {
		  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\n'Inicio:' esperado, mas encontrei: " + lexema );
	  }
  }

  private static void bloco() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  while ( isInicioBloco( token ) ) {
		  if ( token == T_NUM || token == T_PALAVRA ) {
			  declaracao();
		  } else {
			  comando();
		  }
	  }
  }

  private static boolean isInicioBloco( int t ) {
	  return t == T_NUM || t == T_PALAVRA || t == T_ID || t == T_VEM || t == T_SERA_QUE
		  || t == T_CICLO || t == T_ESCREVER || t == T_RETORNA || t == T_FAZ;
  }

  private static void declaracao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  String tipo = tipo();
	  espera( T_DOIS_PONTOS, "':'" );
	  String nome = identificador();
	  declaraVariavel( nome, tipo );

	  if ( token == T_ATRIBUICAO ) {
		  buscaProximoToken();
		  String expr = expressao();
		  geraLinha( nome + " = " + expr );
	  } else {
		  String valorPadrao = tipo.equals( "num" ) ? "0" : "\"\"";
		  geraLinha( nome + " = " + valorPadrao );
	  }
	  acumulaRegraSintaticaReconhecida( "declaracao := tipo ':' IDENTIFICADOR ( '=' expressao )?" );
  }

  private static void comando() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  switch ( token ) {
	  case T_ID:
		  atribuicao();
		  break;
	  case T_VEM:
		  chamadaFuncaoComando();
		  break;
	  case T_SERA_QUE:
		  condicional();
		  break;
	  case T_CICLO:
		  repeticao();
		  break;
	  case T_ESCREVER:
		  saida();
		  break;
	  case T_RETORNA:
		  retorno();
		  break;
	  case T_FAZ:
		  definicaoFuncao();
		  break;
	  default:
		  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nComando nao identificado: " + lexema );
	  }
  }

  private static void atribuicao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  String nome = identificador();
	  verificaVariavelDeclarada( nome );
	  espera( T_ATRIBUICAO, "'='" );
	  String expr = expressao();
	  geraLinha( nome + " = " + expr );
  }

  private static void chamadaFuncaoComando() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  String chamada = chamadaFuncao();
	  geraLinha( chamada );
  }

  private static void saida() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  espera( T_ESCREVER, "'escrever'" );
	  espera( T_ABRE_PAR, "'('" );
	  String expr = expressao();
	  espera( T_FECHA_PAR, "')'" );
	  geraLinha( "print(" + expr + ")" );
  }

  private static void retorno() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  if ( profundidadeFuncao <= 0 ) {
		  registraErroSemantico( "Uso de 'retorna' fora de funcao. Linha: " + linhaAtual );
	  }
	  espera( T_RETORNA, "'retorna'" );
	  espera( T_ABRE_PAR, "'('" );
	  String expr = expressao();
	  espera( T_FECHA_PAR, "')'" );
	  geraLinha( "return " + expr );
  }

  private static void definicaoFuncao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  espera( T_FAZ, "'faz'" );
	  String tipoRetorno = tipo();
	  espera( T_DOIS_PONTOS, "':'" );
	  String nome = identificador();
	  declaraFuncao( nome, tipoRetorno );

	  espera( T_ABRE_PAR, "'('" );
	  String params = parametros();
	  espera( T_FECHA_PAR, "')'" );
	  espera( T_ABRE_CHAVE, "'{'" );

	  geraLinha( "def " + nome + "(" + params + "):" );
	  nivelIdentacao++;
	  iniciaEscopo();
	  adicionaParametrosAoEscopo( params );
	  profundidadeFuncao++;
	  blocoComPassoSeVazio();

	  profundidadeFuncao--;
	  finalizaEscopo();
	  nivelIdentacao--;

	  espera( T_FECHA_CHAVE, "'}'" );
  }

  private static String parametros() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  StringBuilder sb = new StringBuilder();
	  if ( token == T_NUM || token == T_PALAVRA ) {
		  tipo();
		  espera( T_DOIS_PONTOS, "':'" );
		  String nome = identificador();
		  sb.append( nome );
		  while ( token == T_VIRGULA ) {
			  buscaProximoToken();
			  tipo();
			  espera( T_DOIS_PONTOS, "':'" );
			  nome = identificador();
			  sb.append( ", " ).append( nome );
		  }
	  }
	  return sb.toString();
  }

  private static void condicional() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  espera( T_SERA_QUE, "'sera_que'" );
	  espera( T_ABRE_PAR, "'('" );
	  String cond = condicao();
	  espera( T_FECHA_PAR, "')'" );
	  espera( T_ABRE_CHAVE, "'{'" );
	  geraLinha( "if " + cond + ":" );
	  nivelIdentacao++;
	  blocoComPassoSeVazio();
	  nivelIdentacao--;
	  espera( T_FECHA_CHAVE, "'}'" );

	  while ( token == T_E_SE ) {
		  buscaProximoToken();
		  espera( T_ABRE_PAR, "'('" );
		  String condElseIf = condicao();
		  espera( T_FECHA_PAR, "')'" );
		  espera( T_ABRE_CHAVE, "'{'" );
		  geraLinha( "elif " + condElseIf + ":" );
		  nivelIdentacao++;
		  blocoComPassoSeVazio();
		  nivelIdentacao--;
		  espera( T_FECHA_CHAVE, "'}'" );
	  }

	  if ( token == T_SE_NAO ) {
		  buscaProximoToken();
		  espera( T_ABRE_CHAVE, "'{'" );
		  geraLinha( "else:" );
		  nivelIdentacao++;
		  blocoComPassoSeVazio();
		  nivelIdentacao--;
		  espera( T_FECHA_CHAVE, "'}'" );
	  }
  }

  private static void repeticao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  espera( T_CICLO, "'ciclo'" );
	  espera( T_ABRE_PAR, "'('" );
	  String tipo = tipo();
	  espera( T_DOIS_PONTOS, "':'" );
	  String var = identificador();
	  declaraVariavel( var, tipo );
	  espera( T_DE, "'de'" );
	  String inicio = expressao();
	  espera( T_ATE, "'ate'" );
	  String fim = expressao();
	  espera( T_FECHA_PAR, "')'" );
	  espera( T_ABRE_CHAVE, "'{'" );
	  geraLinha( "for " + var + " in range(" + inicio + ", " + fim + " + 1):" );
	  nivelIdentacao++;
	  blocoComPassoSeVazio();
	  nivelIdentacao--;
	  espera( T_FECHA_CHAVE, "'}'" );
  }

  private static void blocoComPassoSeVazio() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  int tamanhoAntes = codigoPython.length();
	  bloco();
	  if ( codigoPython.length() == tamanhoAntes ) {
		  geraLinha( "pass" );
	  }
  }

  private static String condicao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  String esquerda = expressao();
	  String operador = operadorComparacao();
	  String direita = expressao();
	  return esquerda + " " + operador + " " + direita;
  }

  private static String operadorComparacao() throws IOException, ErroLexicoException, ErroSintaticoException {
	  if ( token == T_MAIOR ) {
		  buscaProximoToken();
		  if ( token == T_IGUAL_PALAVRA ) {
			  buscaProximoToken();
			  return ">=";
		  }
		  return ">";
	  }
	  if ( token == T_MENOR ) {
		  buscaProximoToken();
		  if ( token == T_IGUAL_PALAVRA ) {
			  buscaProximoToken();
			  return "<=";
		  }
		  return "<";
	  }
	  if ( token == T_IGUAL_PALAVRA ) {
		  buscaProximoToken();
		  return "==";
	  }
	  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nEsperava um comparador. Encontrei: " + lexema );
	  return "==";
  }

  private static String expressao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  String valor = termo();
	  while ( token == T_MAIS || token == T_MENOS ) {
		  String op = ( token == T_MAIS ) ? "+" : "-";
		  buscaProximoToken();
		  String direito = termo();
		  valor = valor + " " + op + " " + direito;
	  }
	  return valor;
  }

  private static String termo() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  String valor = fator();
	  while ( token == T_VEZES || token == T_DIVIDIDO ) {
		  String op = ( token == T_VEZES ) ? "*" : "/";
		  buscaProximoToken();
		  String direito = fator();
		  valor = valor + " " + op + " " + direito;
	  }
	  return valor;
  }

  private static String fator() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  switch ( token ) {
	  case T_NUMERO:
		  String numero = lexema;
		  buscaProximoToken();
		  return numero;
	  case T_STRING:
		  String str = lexema;
		  buscaProximoToken();
		  if ( str.indexOf( '{' ) >= 0 && str.indexOf( '}' ) >= 0 ) {
			  return "f" + str;
		  }
		  return str;
	  case T_ID:
		  String nome = lexema;
		  verificaVariavelDeclarada( nome );
		  buscaProximoToken();
		  return nome;
	  case T_VEM:
		  return chamadaFuncao();
	  case T_ABRE_CHAVE:
		  buscaProximoToken();
		  String expr = expressao();
		  espera( T_FECHA_CHAVE, "'}'" );
		  return "(" + expr + ")";
	  default:
		  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nExpressao invalida: " + lexema );
		  return "";
	  }
  }

  private static String chamadaFuncao() throws IOException, ErroLexicoException, ErroSintaticoException, ErroSemanticoException {
	  espera( T_VEM, "'vem'" );
	  String nome = identificador();
	  verificaFuncaoDeclarada( nome );
	  espera( T_ABRE_PAR, "'('" );
	  StringBuilder args = new StringBuilder();
	  if ( token != T_FECHA_PAR ) {
		  args.append( expressao() );
		  while ( token == T_VIRGULA ) {
			  buscaProximoToken();
			  args.append( ", " ).append( expressao() );
		  }
	  }
	  espera( T_FECHA_PAR, "')'" );
	  return nome + "(" + args.toString() + ")";
  }

  private static String tipo() throws IOException, ErroLexicoException, ErroSintaticoException {
	  if ( token == T_NUM ) {
		  buscaProximoToken();
		  return "num";
	  }
	  if ( token == T_PALAVRA ) {
		  buscaProximoToken();
		  return "palavra";
	  }
	  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nEsperava tipo 'num' ou 'palavra'. Encontrei: " + lexema );
	  return "num";
  }

  private static String identificador() throws IOException, ErroLexicoException, ErroSintaticoException {
	  if ( token == T_ID ) {
		  String nome = lexema;
		  buscaProximoToken();
		  return nome;
	  }
	  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nEsperava um identificador. Encontrei: " + lexema );
	  return "";
  }

  private static void espera( int esperado, String esperadoTexto ) throws IOException, ErroLexicoException, ErroSintaticoException {
	  if ( token != esperado ) {
		  registraErroSintatico( "Erro Sintatico. Linha: " + linhaAtual + "\nColuna: " + colunaAtual + "\nErro: <" + linhaFonte + ">\nEsperava " + esperadoTexto + ". Encontrei: " + lexema );
	  }
	  buscaProximoToken();
  }

  private static void iniciaPrograma() {
	  codigoPython.setLength( 0 );
	  codigoPython.append( "def main():\n" );
	  nivelIdentacao = 1;
	  escopos.clear();
	  iniciaEscopo();
	  funcoes.clear();
  }

  private static void finalizaPrograma() {
	  if ( codigoPython.length() > 0 ) {
		  codigoPython.append( "\nif __name__ == '__main__':\n" );
		  codigoPython.append( "    main()\n" );
	  }
  }

  private static void iniciaEscopo() {
	  escopos.push( new HashMap<String, String>() );
  }

  private static void finalizaEscopo() {
	  if ( !escopos.isEmpty() ) {
		  escopos.pop();
	  }
  }

  private static void adicionaParametrosAoEscopo( String params ) throws ErroSemanticoException {
	  if ( params == null || params.trim().isEmpty() ) {
		  return;
	  }
	  String[] nomes = params.split( "," );
	  for ( String nome : nomes ) {
		  String trimmed = nome.trim();
		  if ( !trimmed.isEmpty() ) {
			  declaraVariavel( trimmed, "param" );
		  }
	  }
  }

  private static void declaraVariavel( String nome, String tipo ) throws ErroSemanticoException {
	  if ( escopos.isEmpty() ) {
		  iniciaEscopo();
	  }
	  HashMap<String, String> topo = escopos.peek();
	  if ( topo.containsKey( nome ) ) {
		  throw new ErroSemanticoException( "Variavel " + nome + " ja declarada! linha: " + linhaAtual );
	  }
	  topo.put( nome, tipo );
  }

  private static void verificaVariavelDeclarada( String nome ) throws ErroSemanticoException {
	  for ( int i = escopos.size() - 1; i >= 0; i-- ) {
		  if ( escopos.get( i ).containsKey( nome ) ) {
			  return;
		  }
	  }
	  throw new ErroSemanticoException( "Variavel " + nome + " nao esta declarada! linha: " + linhaAtual );
  }

  private static void declaraFuncao( String nome, String tipo ) throws ErroSemanticoException {
	  if ( funcoes.containsKey( nome ) ) {
		  throw new ErroSemanticoException( "Funcao " + nome + " ja declarada! linha: " + linhaAtual );
	  }
	  funcoes.put( nome, tipo );
  }

  private static void verificaFuncaoDeclarada( String nome ) throws ErroSemanticoException {
	  if ( !funcoes.containsKey( nome ) ) {
		  throw new ErroSemanticoException( "Funcao " + nome + " nao esta declarada! linha: " + linhaAtual );
	  }
  }

  private static void geraLinha( String linha ) {
	  codigoPython.append( tabulacao( nivelIdentacao ) );
	  codigoPython.append( linha ).append( "\n" );
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

	  ponteiro++;
	  colunaAtual = ponteiro + 1;
  }

  static char peekChar() {
	  if ( ponteiro >= linhaFonte.length() ) {
		  return FIM_ARQUIVO;
	  }
	  return linhaFonte.charAt( ponteiro );
  }

  static void buscaProximoToken() throws IOException, ErroLexicoException
  {
	  if ( lexema != null ) {
		  // nao usamos ultimoLexema aqui, mas mantemos consistencia
	  }

	  StringBuffer sbLexema = new StringBuffer( "" );

	  // Salta espacos, tabs, enters e comentarios
	  while ( true ) {
		  while ( ( lookAhead == 9 ) || ( lookAhead == '\n' ) || ( lookAhead == 8 ) || ( lookAhead == 11 )
			  || ( lookAhead == 12 ) || ( lookAhead == '\r' ) || ( lookAhead == 32 ) ) {
			  movelookAhead();
		  }

		  if ( lookAhead == '/' && peekChar() == '/' ) {
			  while ( lookAhead != '\n' && lookAhead != FIM_ARQUIVO ) {
				  movelookAhead();
			  }
			  continue;
		  }

		  if ( lookAhead == '/' && peekChar() == '*' ) {
			  movelookAhead();
			  movelookAhead();
			  while ( lookAhead != FIM_ARQUIVO ) {
				  if ( lookAhead == '*' && peekChar() == '/' ) {
					  movelookAhead();
					  movelookAhead();
					  break;
				  }
				  movelookAhead();
			  }
			  continue;
		  }

		  break;
	  }

	  if ( isLetra( lookAhead ) ) {
		  sbLexema.append( lookAhead );
		  movelookAhead();

		  while ( isLetra( lookAhead ) || isDigito( lookAhead ) || lookAhead == '_' ) {
			  sbLexema.append( lookAhead );
			  movelookAhead();
		  }

		  lexema = sbLexema.toString();
		  String lower = lexema.toLowerCase();

		  if ( lower.equals( "inicio" ) )
			  token = T_INICIO;
		  else if ( lower.equals( "acabou" ) )
			  token = T_ACABOU;
		  else if ( lower.equals( "num" ) )
			  token = T_NUM;
		  else if ( lower.equals( "palavra" ) )
			  token = T_PALAVRA;
		  else if ( lower.equals( "faz" ) )
			  token = T_FAZ;
		  else if ( lower.equals( "retorna" ) )
			  token = T_RETORNA;
		  else if ( lower.equals( "escrever" ) )
			  token = T_ESCREVER;
		  else if ( lower.equals( "sera_que" ) )
			  token = T_SERA_QUE;
		  else if ( lower.equals( "e_se" ) )
			  token = T_E_SE;
		  else if ( lower.equals( "se_nao" ) )
			  token = T_SE_NAO;
		  else if ( lower.equals( "ciclo" ) )
			  token = T_CICLO;
		  else if ( lower.equals( "vem" ) )
			  token = T_VEM;
		  else if ( lower.equals( "de" ) )
			  token = T_DE;
		  else if ( lower.equals( "ate" ) )
			  token = T_ATE;
		  else if ( lower.equals( "maior" ) )
			  token = T_MAIOR;
		  else if ( lower.equals( "menor" ) )
			  token = T_MENOR;
		  else if ( lower.equals( "igual" ) )
			  token = T_IGUAL_PALAVRA;
		  else
			  token = T_ID;
	  } else if ( isDigito( lookAhead ) ) {
		  sbLexema.append( lookAhead );
		  movelookAhead();
		  while ( isDigito( lookAhead ) ) {
			  sbLexema.append( lookAhead );
			  movelookAhead();
		  }
		  if ( lookAhead == '.' && isDigito( peekChar() ) ) {
			  sbLexema.append( lookAhead );
			  movelookAhead();
			  while ( isDigito( lookAhead ) ) {
				  sbLexema.append( lookAhead );
				  movelookAhead();
			  }
		  }
		  token = T_NUMERO;
	  } else if ( lookAhead == '"' ) {
		  sbLexema.append( lookAhead );
		  movelookAhead();
		  while ( lookAhead != '"' && lookAhead != FIM_ARQUIVO ) {
			  if ( lookAhead == '\\' ) {
				  sbLexema.append( lookAhead );
				  movelookAhead();
				  if ( lookAhead == FIM_ARQUIVO ) {
					  break;
				  }
			  }
			  sbLexema.append( lookAhead );
			  movelookAhead();
		  }
		  if ( lookAhead == '"' ) {
			  sbLexema.append( lookAhead );
			  movelookAhead();
			  token = T_STRING;
		  } else {
			  token = T_ERRO_LEX;
		  }
	  } else if ( lookAhead == '(' ) {
		  sbLexema.append( lookAhead );
		  token = T_ABRE_PAR;
		  movelookAhead();
	  } else if ( lookAhead == ')' ) {
		  sbLexema.append( lookAhead );
		  token = T_FECHA_PAR;
		  movelookAhead();
	  } else if ( lookAhead == '{' ) {
		  sbLexema.append( lookAhead );
		  token = T_ABRE_CHAVE;
		  movelookAhead();
	  } else if ( lookAhead == '}' ) {
		  sbLexema.append( lookAhead );
		  token = T_FECHA_CHAVE;
		  movelookAhead();
	  } else if ( lookAhead == ':' ) {
		  sbLexema.append( lookAhead );
		  token = T_DOIS_PONTOS;
		  movelookAhead();
	  } else if ( lookAhead == ',' ) {
		  sbLexema.append( lookAhead );
		  token = T_VIRGULA;
		  movelookAhead();
	  } else if ( lookAhead == '=' ) {
		  sbLexema.append( lookAhead );
		  token = T_ATRIBUICAO;
		  movelookAhead();
	  } else if ( lookAhead == '+' ) {
		  sbLexema.append( lookAhead );
		  token = T_MAIS;
		  movelookAhead();
	  } else if ( lookAhead == '-' ) {
		  sbLexema.append( lookAhead );
		  token = T_MENOS;
		  movelookAhead();
	  } else if ( lookAhead == '*' ) {
		  sbLexema.append( lookAhead );
		  token = T_VEZES;
		  movelookAhead();
	  } else if ( lookAhead == '/' ) {
		  sbLexema.append( lookAhead );
		  token = T_DIVIDIDO;
		  movelookAhead();
	  } else if ( lookAhead == FIM_ARQUIVO ) {
		  token = T_FIM_FONTE;
	  } else {
		  token = T_ERRO_LEX;
		  sbLexema.append( lookAhead );
		  movelookAhead();
	  }

	  lexema = sbLexema.toString();
	  mostraToken();

	  if ( token == T_ERRO_LEX ) {
		  mensagemDeErro = "Erro Lexico na linha: " + linhaAtual + "\nReconhecido ao atingir a coluna: " + colunaAtual + "\nLinha do Erro: <" + linhaFonte + ">\nToken desconhecido: " + lexema;
		  throw new ErroLexicoException( mensagemDeErro );
	  }
  }

  static boolean isLetra( char c ) {
	  return ( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' );
  }

  static boolean isDigito( char c ) {
	  return c >= '0' && c <= '9';
  }

  static void mostraToken()
  {
	  StringBuffer tokenLexema = new StringBuffer( "" );

	  switch ( token ) {
	  case T_INICIO: tokenLexema.append( "T_INICIO" ); break;
	  case T_ACABOU: tokenLexema.append( "T_ACABOU" ); break;
	  case T_NUM: tokenLexema.append( "T_NUM" ); break;
	  case T_PALAVRA: tokenLexema.append( "T_PALAVRA" ); break;
	  case T_FAZ: tokenLexema.append( "T_FAZ" ); break;
	  case T_RETORNA: tokenLexema.append( "T_RETORNA" ); break;
	  case T_ESCREVER: tokenLexema.append( "T_ESCREVER" ); break;
	  case T_SERA_QUE: tokenLexema.append( "T_SERA_QUE" ); break;
	  case T_E_SE: tokenLexema.append( "T_E_SE" ); break;
	  case T_SE_NAO: tokenLexema.append( "T_SE_NAO" ); break;
	  case T_CICLO: tokenLexema.append( "T_CICLO" ); break;
	  case T_VEM: tokenLexema.append( "T_VEM" ); break;
	  case T_DE: tokenLexema.append( "T_DE" ); break;
	  case T_ATE: tokenLexema.append( "T_ATE" ); break;
	  case T_MAIOR: tokenLexema.append( "T_MAIOR" ); break;
	  case T_MENOR: tokenLexema.append( "T_MENOR" ); break;
	  case T_IGUAL_PALAVRA: tokenLexema.append( "T_IGUAL_PALAVRA" ); break;
	  case T_DOIS_PONTOS: tokenLexema.append( "T_DOIS_PONTOS" ); break;
	  case T_ABRE_PAR: tokenLexema.append( "T_ABRE_PAR" ); break;
	  case T_FECHA_PAR: tokenLexema.append( "T_FECHA_PAR" ); break;
	  case T_ABRE_CHAVE: tokenLexema.append( "T_ABRE_CHAVE" ); break;
	  case T_FECHA_CHAVE: tokenLexema.append( "T_FECHA_CHAVE" ); break;
	  case T_VIRGULA: tokenLexema.append( "T_VIRGULA" ); break;
	  case T_ATRIBUICAO: tokenLexema.append( "T_ATRIBUICAO" ); break;
	  case T_MAIS: tokenLexema.append( "T_MAIS" ); break;
	  case T_MENOS: tokenLexema.append( "T_MENOS" ); break;
	  case T_VEZES: tokenLexema.append( "T_VEZES" ); break;
	  case T_DIVIDIDO: tokenLexema.append( "T_DIVIDIDO" ); break;
	  case T_NUMERO: tokenLexema.append( "T_NUMERO" ); break;
	  case T_STRING: tokenLexema.append( "T_STRING" ); break;
	  case T_ID: tokenLexema.append( "T_ID" ); break;
	  case T_FIM_FONTE: tokenLexema.append( "T_FIM_FONTE" ); break;
	  case T_ERRO_LEX: tokenLexema.append( "T_ERRO_LEX" ); break;
	  case T_NULO: tokenLexema.append( "T_NULO" ); break;
	  default: tokenLexema.append( "N/A" ); break;
	  }

	  System.out.println( tokenLexema.toString() + " ( " + lexema + " )" );
	  acumulaToken( tokenLexema.toString() + " ( " + lexema + " )" );
	  tokenLexema.append( lexema );
  }
  
  private static void abreArquivo() {

		JFileChooser fileChooser = new JFileChooser( getProjectDirectory() );
		
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

	private static File getProjectDirectory() {
		File baseDir = null;
		try {
			File location = new File( Semantico.class.getProtectionDomain().getCodeSource().getLocation().toURI() );
			baseDir = location.isFile() ? location.getParentFile() : location;
		} catch ( Exception e ) {
			baseDir = null;
		}

		File resolved = resolveProjectRoot( baseDir );
		if ( resolved != null ) {
			return resolved;
		}

		String userDir = System.getProperty( "user.dir" );
		if ( userDir == null || userDir.trim().isEmpty() ) {
			return new File( "." );
		}
		return new File( userDir );
	}

	private static File resolveProjectRoot( File startDir ) {
		File current = startDir;
		for ( int i = 0; i < 6 && current != null; i++ ) {
			File compiladorDir = new File( current, "compilador" );
			File testsDir = new File( current, "tests" );
			File grammarFile = new File( current, "Gramatica.md" );
			if ( compiladorDir.isDirectory() && testsDir.isDirectory() && grammarFile.isFile() ) {
				return current;
			}
			current = current.getParentFile();
		}
		return null;
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

		JFileChooser fileChooser = new JFileChooser( getProjectDirectory() );
			
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

    static void registraErroSemantico( String msg ) throws ErroSemanticoException {
        if ( estadoCompilacao == E_SEM_ERROS ) {
            estadoCompilacao = E_ERRO_SEMANTICO;
            mensagemDeErro = msg;
        }
        throw new ErroSemanticoException( msg );
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
