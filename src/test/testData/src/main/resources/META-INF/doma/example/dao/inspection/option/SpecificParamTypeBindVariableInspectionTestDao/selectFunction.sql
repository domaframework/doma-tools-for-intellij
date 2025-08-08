SELECT e.id
       , e.name
       , e.age
       , e.department
       , e.salary
  FROM employee e
 WHERE e.id = /* id */1
   /*%if employee != null */
   AND e.department = /* <error descr="The bind variable [function] does not exist in the DAO method [selectFunction]">function</error> */'Engineering'
   /*%end*/
 ORDER BY e.salary DESC
