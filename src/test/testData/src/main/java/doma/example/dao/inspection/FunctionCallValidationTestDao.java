package doma.example.dao.inspection;

import doma.example.entity.*;
import org.seasar.doma.*;

import java.util.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Dao
public interface FunctionCallValidationTestDao {

    @Select
    List<Project> testValidParameter(float floatValue, int intValue
            , Employee employee
            , Project project, DummyProject subProject
            , LocalDate localDate, LocalDateTime localDateTime
            , CharSequence charSeq, String str
            , List<ColumnEntity> columns);

    @Select
    List<Project> testInvalidParameterCount(Employee employee, Project project, List<ColumnEntity> columns);

    @Select
    List<User> testInvalidParameterTypes(Employee employee, Project project, DummyProject subProject, List<ColumnEntity> columns);
}