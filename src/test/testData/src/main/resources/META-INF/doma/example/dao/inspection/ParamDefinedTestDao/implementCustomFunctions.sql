SELECT
    e.employee_id
    , u.user_id
    , u.user_name
  FROM user u
 WHERE p.employee_id = /* employee.employeeId */0
   AND p.user_id = /* employee.userId */0
    OR is_gest = /* @isGest() */false
    OR flag = /* @<error descr="Custom function [authUser] not found in class [doma.example.expression.TestExpressionFunctions]">authUser</error>() */false
   AND lang = /* @<error descr="Custom function [getLangCode] not found in class [doma.example.expression.TestExpressionFunctions]">getLangCode</error>() */'en'