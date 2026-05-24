%language "Java"
%define api.parser.class {TacBisonParser}
%define api.value.type {Object}
%define lex_throws {}
%define parse.error verbose

%code imports {
import java.util.ArrayList;
import java.util.List;
}

%code {
    private TacProgram result;

    TacProgram getResult() {
        return result;
    }

    private static String tokenValue(Object value) {
        return ((Token) value).value;
    }

    private static String tokenLexeme(Object value) {
        return ((Token) value).lexeme;
    }

    private static String relop(Object value) {
        return tokenValue(value);
    }

    private static List<TacStatement> newStatementList(TacStatement statement) {
        List<TacStatement> statements = new ArrayList<>();
        statements.add(statement);
        return statements;
    }

    @SuppressWarnings("unchecked")
    private static List<TacStatement> statementList(Object value) {
        return (List<TacStatement>) value;
    }

    private static List<TacStatement> appendStatement(Object value, TacStatement statement) {
        List<TacStatement> statements = statementList(value);
        statements.add(statement);
        return statements;
    }

    private static RuntimeException syntaxError(String message) {
        return new RuntimeException(message);
    }
}

%token IDN DEC OCT HEX
%token IF THEN ELSE WHILE DO BEGIN END
%token ADD SUB MUL DIV EQ GT LT GE LE NEQ SLP SRP SEMI
%token ILOCT ILHEX ILNUM UNKNOWN

%left ADD SUB
%left MUL DIV
%nonassoc THEN
%nonassoc ELSE

%start program

%%

program
    : top_statements opt_semi
      {
          result = new TacProgram(statementList($1));
      }
    ;

top_statements
    : statement
      {
          $$ = newStatementList((TacStatement) $1);
      }
    | top_statements SEMI statement
      {
          $$ = appendStatement($1, (TacStatement) $3);
      }
    ;

opt_semi
    : %empty
    | SEMI
    ;

statement
    : IDN EQ expr
      {
          $$ = new TacAssign(tokenValue($1), (TacExpr) $3);
      }
    | error
      {
          $$ = new TacError();
      }
    | IF condition THEN statement %prec THEN
      {
          $$ = new TacIf((TacCondition) $2, (TacStatement) $4, null);
      }
    | IF condition THEN statement ELSE statement
      {
          $$ = new TacIf((TacCondition) $2, (TacStatement) $4, (TacStatement) $6);
      }
    | WHILE condition DO statement
      {
          $$ = new TacWhile((TacCondition) $2, (TacStatement) $4);
      }
    | BEGIN compound_statements END
      {
          $$ = new TacCompound(statementList($2));
      }
    ;

compound_statements
    : statement SEMI
      {
          $$ = newStatementList((TacStatement) $1);
      }
    | compound_statements statement SEMI
      {
          $$ = appendStatement($1, (TacStatement) $2);
      }
    ;

condition
    : expr relop expr
      {
          $$ = new TacCondition((TacExpr) $1, (String) $2, (TacExpr) $3);
      }
    ;

relop
    : GT
      {
          $$ = relop($1);
      }
    | LT
      {
          $$ = relop($1);
      }
    | EQ
      {
          $$ = relop($1);
      }
    | GE
      {
          $$ = relop($1);
      }
    | LE
      {
          $$ = relop($1);
      }
    | NEQ
      {
          $$ = relop($1);
      }
    ;

expr
    : expr ADD expr
      {
          $$ = new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
      }
    | expr SUB expr
      {
          $$ = new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
      }
    | expr MUL expr
      {
          $$ = new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
      }
    | expr DIV expr
      {
          $$ = new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
      }
    | SLP expr SRP
      {
          $$ = $2;
      }
    | factor
      {
          $$ = $1;
      }
    ;

factor
    : IDN
      {
          $$ = new TacValue(tokenValue($1));
      }
    | DEC
      {
          $$ = new TacValue(tokenValue($1));
      }
    | OCT
      {
          $$ = new TacValue(tokenValue($1));
      }
    | HEX
      {
          $$ = new TacValue(tokenValue($1));
      }
    | ILOCT
      {
          throw syntaxError("非法八进制整数: " + tokenLexeme($1));
      }
    | ILHEX
      {
          throw syntaxError("非法十六进制整数: " + tokenLexeme($1));
      }
    | ILNUM
      {
          throw syntaxError("非法整数: " + tokenLexeme($1));
      }
    | UNKNOWN
      {
          throw syntaxError("无法识别的字符: '" + tokenLexeme($1) + "'");
      }
    ;

%%
