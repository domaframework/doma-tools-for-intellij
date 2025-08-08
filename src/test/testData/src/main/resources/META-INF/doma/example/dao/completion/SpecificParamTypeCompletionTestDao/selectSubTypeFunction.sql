SELECT e.id
       , e.name
       , e.age
       , e.department
       , e.salary
       , e.hire_date
  FROM employee e
 WHERE e.id = /* <caret> */1
   /*%if employee != null */
   AND e.name = /* employee.name */'John'
   AND e.age >= /* employee.age */25
   /*%end*/
 ORDER BY e.hire_date ASC
