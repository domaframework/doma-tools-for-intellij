SELECT e.id
       , e.name
       , e.age
       , e.department
  FROM employee e
 WHERE e.id = /* <caret> */1
   AND e.name = /* employee.name */'John'
   AND e.age > /* employee.age */20
