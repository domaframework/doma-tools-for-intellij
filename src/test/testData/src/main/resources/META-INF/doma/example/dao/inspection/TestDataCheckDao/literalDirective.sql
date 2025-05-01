SELECT e.employee_id AS employeeId
       , e.employee_name AS employeeName
       , e.department_name AS departmentName
  FROM employee e
 WHERE e.employee_id = <error descr="Bind variables must be followed by test data">/*^ literalId */</error>
   AND e.employee_name = <error descr="Bind variables must be followed by test data">/*^ literalName */</error>
   AND e.age >= /*^ literalAge */99
