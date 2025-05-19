SELECT
    e.employee_id
    , u.user_id
    , u.user_name
  FROM user u
 WHERE p.employee_id = /* employee.employeeId */0
   AND p.user_id = /* employee.userId */0
    OR is_gest = /* @isGest() */false
    OR flag = /* @<error descr="The function [authUser] is not defined in the registered custom function classes">authUser</error>() */false
   AND lang = /* @<error descr="The function [getLangCode] is not defined in the registered custom function classes">getLangCode</error>() */'en'