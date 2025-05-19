package doma.example.expression;

import org.seasar.doma.jdbc.dialect.StandardDialect;

// doma.example.expression.TestExpressionFunctions
public class TestExpressionFunctions extends StandardDialect.StandardExpressionFunctions {

  private static final char DEFAULT_ESCAPE_CHAR = '\\';

  public TestExpressionFunctions() {
    super(DEFAULT_ESCAPE_CHAR, null);
  }

  public TestExpressionFunctions(char[] wildcards) {
    super(wildcards);
  }

  protected TestExpressionFunctions(char escapeChar, char[] wildcards) {
    super(escapeChar, wildcards);
  }

  public Integer userId() {
    return 10000000;
  }

  public String userName() {
    return "userName";
  }

  public Integer userAge() {
    return 99;
  }

  public String langCode() {
    return "ja";
  }

  public boolean isGest() {
    return true;
  }
}