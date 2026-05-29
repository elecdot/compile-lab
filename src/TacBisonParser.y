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

    private static boolean isInvalidExpr(Object value) {
        return value instanceof TacInvalidExpr;
    }

    private static boolean hasInvalidExpr(TacCondition condition) {
        return condition.left instanceof TacInvalidExpr || condition.right instanceof TacInvalidExpr;
    }
}

/*
 • 标识符（IDN）、十进制整数（DEC）、八进制整数（OCT）、十六进制整数（HEX）。
 • 关键字：IF、THEN、ELSE、WHILE、DO、BEGIN、END。
 • 运算符和分隔符：+（ADD）、-（SUB）、*（MUL）、/（DIV）、=（赋予
   EQ）、>（GT）、<（LT）、>=（GE）、<=（LE）、<>（NEQ）、(（SLP）、)（SRP）、;（SEMI）。
 • 错误
   token：ILOCT（非法八进制）、ILHEX（非法十六进制）、ILNUM（非法数字）、UNKNOWN（无法识别的字符）——它们被
   用来触发语法错误。*/
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

// 允许程序**末尾**(只有末尾)有一个可选的分号，方便用户习惯
opt_semi
    : %empty
    | SEMI
    ;

// S
statement
    : IDN EQ expr
      {
          $$ = isInvalidExpr($3) ? new TacError() : new TacAssign(tokenValue($1), (TacExpr) $3);
      }
    | error
      {
          $$ = new TacError();
      }
    | IF condition THEN statement %prec THEN
      {
          TacCondition condition = (TacCondition) $2;
          $$ = hasInvalidExpr(condition) ? new TacError() : new TacIf(condition, (TacStatement) $4, null);
      }
    | IF condition THEN statement ELSE statement
      {
          TacCondition condition = (TacCondition) $2;
          $$ = hasInvalidExpr(condition) ? new TacError() : new TacIf(condition, (TacStatement) $4, (TacStatement) $6);
      }
    | WHILE condition DO statement
      {
          TacCondition condition = (TacCondition) $2;
          $$ = hasInvalidExpr(condition) ? new TacError() : new TacWhile(condition, (TacStatement) $4);
      }
    | BEGIN compound_statements END
      {
          $$ = new TacCompound(statementList($2));
      }
    ;

// L
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

// C
condition
    : expr relop expr
      {
          $$ = new TacCondition((TacExpr) $1, (String) $2, (TacExpr) $3);
      }
    ;

// 关系运算符
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

// E
expr
    : expr ADD expr
      {
          $$ = isInvalidExpr($1) || isInvalidExpr($3)
              ? new TacInvalidExpr()
              : new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
      }
    | expr SUB expr
      {
          $$ = isInvalidExpr($1) || isInvalidExpr($3)
              ? new TacInvalidExpr()
              : new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
      }
    | expr MUL expr
      {
          $$ = isInvalidExpr($1) || isInvalidExpr($3)
              ? new TacInvalidExpr()
              : new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
      }
    | expr DIV expr
      {
          $$ = isInvalidExpr($1) || isInvalidExpr($3)
              ? new TacInvalidExpr()
              : new TacBinary((TacExpr) $1, tokenValue($2), (TacExpr) $3);
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
          yyerror("非法八进制整数: " + tokenLexeme($1));
          $$ = new TacInvalidExpr();
      }
    | ILHEX
      {
          yyerror("非法十六进制整数: " + tokenLexeme($1));
          $$ = new TacInvalidExpr();
      }
    | ILNUM
      {
          yyerror("非法整数: " + tokenLexeme($1));
          $$ = new TacInvalidExpr();
      }
    | UNKNOWN
      {
          yyerror("无法识别的字符: '" + tokenLexeme($1) + "'");
          $$ = new TacInvalidExpr();
      }
    ;

%%
