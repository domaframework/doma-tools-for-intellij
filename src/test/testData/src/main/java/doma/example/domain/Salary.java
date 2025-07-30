package doma.example.domain;

import java.math.BigDecimal;
import org.seasar.doma.DataType;

@DataType
public record Salary(BigDecimal value) {
}