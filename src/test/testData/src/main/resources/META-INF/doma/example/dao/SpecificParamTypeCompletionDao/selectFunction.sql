SELECT e.id
       , e.name
       , e.age
       , e.department
       , e.salary
  FROM employee e
 WHERE e.id = /* <caret> */1
   /*%if employee != null */
   AND e.department = /* employee.department */'Engineering'
   /*%end*/
 ORDER BY e.salary DESC
