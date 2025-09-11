-- Check BindVariable Definition In Function Parameters
SELECT
e.employee_id
    , u.user_id
    , u.user_name
FROM user u
WHERE p.employee_id = /* employee.<error descr="Function employeeParam parameter type mismatch. Actual types: (Employee, Integer)Definition candidates:  employeeParam(String, Integer)  employeeParam(int, Float)">employeeParam</error>(employee.<error descr="The field or method [dist] does not exist in the class [Employee]">dist</error>, employee.employeeId) */0
AND p.base_rank = /* employee.<error descr="Function employeeParam parameter type mismatch. Actual types: (Integer)Definition candidates:  employeeParam(String, Integer)  employeeParam(int, Float)">employeeParam</error>(user.userId, <error descr="The bind variable [count] does not exist in the DAO method [bindVariableInFunctionParameters]">count</error>) */0
/*%if employee != null && (employee.employeeId > 100 || employee.employeeId < 50) */
   AND p.employee_id = /* employee.<error descr="Function employeeParam parameter type mismatch. Actual types: (Employee)Definition candidates:  employeeParam(String, Integer)  employeeParam(int, Float)">employeeParam</error>(employee.<error descr="The field or method [dist] does not exist in the class [Employee]">dist</error>, <error descr="The bind variable [rank] does not exist in the DAO method [bindVariableInFunctionParameters]">rank</error>) */0
OR flag = /* @isNotBlank("string") */false
/*%end */