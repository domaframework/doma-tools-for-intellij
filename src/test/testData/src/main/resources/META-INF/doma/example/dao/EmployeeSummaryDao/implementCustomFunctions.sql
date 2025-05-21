SELECT
    e.employee_id
    , u.user_id
    , u.user_name
  FROM user u
 WHERE p.employee_id = /* employee.employeeId */0
   AND p.user_id = /* employee.userId */0
    OR is_gest = /* @isGest() */false
    OR flag = /* @<error descr="An undefined built-in or custom function [authUser] is being called">authUser</error>() */false
   AND lang = /* @<error descr="An undefined built-in or custom function [getLangCode] is being called">getLangCode</error>() */'en'