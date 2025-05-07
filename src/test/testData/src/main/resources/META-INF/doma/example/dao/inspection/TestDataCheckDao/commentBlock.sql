-- This Comment Line
SELECT e.employee_id AS employeeId
       , e.employee_name AS employeeName
       , e.department_name AS departmentName
  /**
    This is a comment block
  */
  FROM employee e
 /*%! This comment will be removed */
 WHERE e.employee_id = <error descr="Bind variables must be followed by test data">/*^ id */</error>
   AND e.age >= /*^ literalAge */99
   AND e.sub_id IN /* subIds */(1,2,3)