-- Check BindVariable Definition In Function Parameters
SELECT
    e.employee_id
    , u.user_id
    , u.user_name
FROM user u
WHERE p.employee_id = /* employee.employeeParam(employee.<error descr="The field or method [dist] does not exist in the class [Employee]">dist</error>, employee.employeeId) */0
and p.base_rank = /* employee.employeeParam(user.userId, <error descr="The bind variable [count] does not exist in the Dao method [bindVariableInFunctionParameters]">count</error>) */0
/*%end */