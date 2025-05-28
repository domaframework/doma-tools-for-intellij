SELECT e.employee_id AS employeeId
       , e.employee_name AS employeeName
       , e.department_name AS departmentName
  FROM employee e
 WHERE e.employee_id = <error descr="Test data is required after a bind variable directive or a literal variable directive">/*^ literalId */</error>
   AND e.employee_name = <error descr="Test data is required after a bind variable directive or a literal variable directive">/*^ literalName */</error>
   AND e.age >= /*^ literalAge */99
   AND e.use = /* literalTrue */TRUE
   AND e.use = /* literalFalse */false
   AND e.use = /* literalNull */Null
