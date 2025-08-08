SELECT e.id
       , e.name
       , e.age
       , e.department
       , e.salary
       , e.hire_date
  FROM employee e
 WHERE e.id = /* id */1
   /*%if employee != null */
   AND e.name = /* employee.employeeName */'John'
   AND e.age >= /* <error descr="The bind variable [function] does not exist in the DAO method [selectSubTypeFunction]">function</error> */25
   /*%end*/
 ORDER BY e.hire_date ASC
