package org.domaframework.doma.intellij;

import java.util.Set;
import java.util.HashSet;

import com.intellij.lexer.FlexLexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import org.domaframework.doma.intellij.psi.SqlTypes;

%%

%class SqlLexer
%public
%implements FlexLexer
%unicode
%function advance
%type IElementType
%{
  // SQL keywords
  private static final Set<String> KEYWORDS = Set.of(
      "add",
      "after",
      "alter",
      "all",
      "and",
      "as",
      "asc",
      "between",
      "breadth",
      "by",
      "case",
      "change",
      "check",
      "conflict",
      "constraint",
      "column",
      "comment",
      "create",
      "cross",
      "cycle",
      "database",
      "default",
      "delete",
      "desc",
      "distinct",
      "do",
      "drop",
      "else",
      "end",
      "except",
      "exists",
      "first",
      "foreign",
      "from",
      "full",
      "group",
      "having",
      "if",
      "in",
      "index",
      "inner",
      "insert",
      "intersect",
      "into",
      "is",
      "join",
      "key",
      "left",
      "like",
      "limit",
      "not",
      "nothing",
      "null",
      "materialized",
      "modify",
      "offset",
      "on",
      "outer",
      "or",
      "order",
      "primary",
      "references",
      "rename",
      "returning",
      "recursive",
      "right",
      "search",
      "select",
      "set",
      "table",
      "temporary",
      "then",
      "to",
      "truncate",
      "union",
      "unique",
      "update",
      "using",
      "values",
      "view",
      "when",
      "where",
      "with"
  );

  // COLUMN DataTypes
  private static final Set<String> DATATYPES = Set.of(
      "int",
      "integer",
      "smallint",
      "bigint",
      "tinyint",
      "float",
      "double",
      "decimal",
      "numeric",
      "char",
      "varchar",
      "text",
      "date",
      "time",
      "timestamp",
      "datetime",
      "boolean",
      "bit",
      "binary",
      "varbinary",
      "blob",
      "clob",
      "json",
      "enum",
      "set"
   );

  private static boolean isKeyword(CharSequence word) {
    // TODO Reads plugin settings and allows users to register arbitrary keywords
      return KEYWORDS.contains(word.toString().toLowerCase());
  }

  private static boolean isColumnDataType(CharSequence word) {
    return DATATYPES.contains(word.toString().toLowerCase());
  }
%}

%eof{  return;
%eof}

// common
LineTerminator = \R
WhiteSpace = [ \n\t\f]
BlockCommentContent = ([^*]|\*+[^/])+

// SQL tokens
BlockCommentStart = "/*"
BlockCommentEnd = "*/"
LineComment = "--" .* {LineTerminator}?
Word = [:jletterdigit:]+
Number = \d+
String = \'((\'\')|[^\'])*\'

// EL tokens
El_Number = \d+(L|(\.\d+)?[FDB])?
El_String = \"((\"\")|[^\"])*\"
El_Char = \'.\'
El_Identifier = [:jletter:][:jletterdigit:]*
El_NonWordPart = [=<>\-,/*();\R \n\t\f]

%state EXPRESSION DIRECTIVE BLOCK_COMMENT PARSER_LEVEL_COMMENT

%%

<YYINITIAL> {
 {BlockCommentStart}/[%#\^]                   { yybegin(DIRECTIVE); return SqlTypes.BLOCK_COMMENT_START; }
 {BlockCommentStart}/[@\"' \n\t\f[:jletter:]] { yybegin(EXPRESSION); return SqlTypes.BLOCK_COMMENT_START; }
 {BlockCommentStart}                          { yybegin(BLOCK_COMMENT); return SqlTypes.BLOCK_COMMENT_START;}
  {LineComment}                                { return SqlTypes.LINE_COMMENT; }
  {String}                                     { return SqlTypes.STRING; }
  {Number}                                     { return SqlTypes.NUMBER; }
  {Word}                                       { return isKeyword(yytext()) ? SqlTypes.KEYWORD : isColumnDataType(yytext()) ? SqlTypes.DATATYPE : SqlTypes.WORD; }
  "."                                         { return SqlTypes.DOT; }
  ","                                         { return SqlTypes.COMMA; }
  "+"                                          { return SqlTypes.PLUS;}
  "-"                                          { return SqlTypes.MINUS;}
  "*"                                          { return SqlTypes.ASTERISK;}
  "/"                                          { return SqlTypes.SLASH;}
  "%"                                          { return SqlTypes.PERCENT;}
  "("                                          { return SqlTypes.LEFT_PAREN; }
  ")"                                          { return SqlTypes.RIGHT_PAREN; }
  "<"                                          { return SqlTypes.LT;}
  "<="                                         { return SqlTypes.LE;}
  ">"                                          { return SqlTypes.GT;}
  ">="                                         { return SqlTypes.GE;}
  "true"                                       { return SqlTypes.BOOLEAN;}
  "false"                                      { return SqlTypes.BOOLEAN;}
  ({LineTerminator}|{WhiteSpace})+             { return TokenType.WHITE_SPACE; }
  [^]                                          { return SqlTypes.OTHER; }
}

<EXPRESSION> {
  {BlockCommentEnd}                            { yybegin(YYINITIAL); return SqlTypes.BLOCK_COMMENT_END; }
  ":"                                          { return SqlTypes.SEPARATOR; }
  "."                                          { return SqlTypes.DOT; }
  ","                                          { return SqlTypes.COMMA; }
  "("                                          { return SqlTypes.LEFT_PAREN; }
  ")"                                          { return SqlTypes.RIGHT_PAREN; }
  "@"                                          { return SqlTypes.AT_SIGN; }
  "+"                                          { return SqlTypes.PLUS;}
  "-"                                          { return SqlTypes.MINUS;}
  "*"                                          { return SqlTypes.ASTERISK;}
  "/"                                          { return SqlTypes.SLASH;}
  "%"                                          { return SqlTypes.PERCENT;}
  "=="                                         { return SqlTypes.EL_EQ;}
  "!="                                         { return SqlTypes.EL_NE;}
  "<"                                          { return SqlTypes.LT;}
  "<="                                         { return SqlTypes.LE;}
  ">"                                          { return SqlTypes.GT;}
  ">="                                         { return SqlTypes.GE;}
  "!"                                          { return SqlTypes.EL_NOT;}
  "&&"                                         { return SqlTypes.EL_AND;}
  "||"                                         { return SqlTypes.EL_OR;}
  "new"                                        { return SqlTypes.EL_NEW;}
  "null"                                       { return SqlTypes.EL_NULL;}
  "true"                                       { return SqlTypes.BOOLEAN;}
  "false"                                      { return SqlTypes.BOOLEAN;}
  {El_Number}                                  { return SqlTypes.EL_NUMBER; }
  {El_String}                                  { return SqlTypes.EL_STRING; }
  {El_Char}                                    { return SqlTypes.EL_CHAR; }
  {El_Identifier}                              { return SqlTypes.EL_IDENTIFIER; }
  ({LineTerminator}|{WhiteSpace})+             { return TokenType.WHITE_SPACE; }
  [^]                                          { return TokenType.BAD_CHARACTER; }
}

<DIRECTIVE> {
  {BlockCommentEnd}                            { yybegin(YYINITIAL); return SqlTypes.BLOCK_COMMENT_END; }
  "%if"/{El_NonWordPart}                       { yybegin(EXPRESSION); return SqlTypes.EL_IF; }
  "%elseif"/{El_NonWordPart}                   { yybegin(EXPRESSION); return SqlTypes.EL_ELSEIF; }
  "%else"/{El_NonWordPart}                     { yybegin(EXPRESSION); return SqlTypes.EL_ELSE; }
  "%for"/{El_NonWordPart}                      { yybegin(EXPRESSION); return SqlTypes.EL_FOR; }
  "%expand"/{El_NonWordPart}                   { yybegin(EXPRESSION); return SqlTypes.EL_EXPAND; }
  "%populate"/{El_NonWordPart}                 { yybegin(EXPRESSION); return SqlTypes.EL_POPULATE; }
  "%end"/{El_NonWordPart}                      { yybegin(EXPRESSION); return SqlTypes.EL_END; }
  "%!"                                         { yybegin(PARSER_LEVEL_COMMENT); return SqlTypes.EL_PARSER_LEVEL_COMMENT; }
  "#"                                          { yybegin(EXPRESSION); return SqlTypes.HASH; }
  "^"                                          { yybegin(EXPRESSION); return SqlTypes.CARET; }
  ({LineTerminator}|{WhiteSpace})+             { return TokenType.WHITE_SPACE; }
  [^]                                          { return TokenType.BAD_CHARACTER; }
}

<BLOCK_COMMENT> {
  {BlockCommentEnd}                            { yybegin(YYINITIAL); return SqlTypes.BLOCK_COMMENT_END; }
  {BlockCommentContent}                        { return SqlTypes.BLOCK_COMMENT_CONTENT; }
  [^]                                          { return TokenType.BAD_CHARACTER; }
}

<PARSER_LEVEL_COMMENT> {
  {BlockCommentEnd}                            { yybegin(YYINITIAL); return SqlTypes.BLOCK_COMMENT_END; }
  {BlockCommentContent}                        { return SqlTypes.BLOCK_COMMENT_CONTENT; }
  [^]                                          { return TokenType.BAD_CHARACTER; }
}