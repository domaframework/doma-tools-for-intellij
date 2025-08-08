SELECT p.id
       , p.name
       , p.description
  FROM project p
       INNER JOIN employee e
               ON p.employee_id = e.id
 WHERE e.id = /* employee.id */1
   /*%if searchName != null */
   AND p.name LIKE /* <caret> */'%test%'
   /*%end*/
