package doma.example.expression;

import doma.example.entity.Employee;
import doma.example.entity.Project;
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

    public boolean isGuest() {
        return true;
    }

    public boolean isGuest(Employee employee) {
        return employee.employeeId != null;
    }

    public boolean isGuest(Project project) {
        return project.getEmployeeName(0).isBlank();
    }

    public Integer customCalculate(Integer base, Integer multiplier) {
        return base * multiplier;
    }

    public String customFormat(String prefix, String suffix) {
        return prefix + "_" + suffix;
    }

    public Double customCompute(Double value, Integer factor) {
        return value * factor;
    }

    public boolean isGuestInProject(Project project) {
        return project.getEmployeeName(0).equals(userName());
    }
}