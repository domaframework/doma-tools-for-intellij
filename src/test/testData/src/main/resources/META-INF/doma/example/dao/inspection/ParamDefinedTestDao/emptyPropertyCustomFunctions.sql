SELECT
       e.employee_id
       , u.user_id
       , u.user_name
  FROM user u
 WHERE p.employee_id = /* employee.employeeId */0
   AND p.user_id = /* employee.userId */0
    OR is_gest = /* @<error descr="An invalid ExpressionFunctions implementation class is configured in doma.compile.config:[]">isManager</error>() */false
    OR flag = /* @<error descr="An invalid ExpressionFunctions implementation class is configured in doma.compile.config:[]">authUser</error>() */false
   AND lang = /* @<error descr="An invalid ExpressionFunctions implementation class is configured in doma.compile.config:[]">isBlank</error>() */'en'
