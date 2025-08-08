SELECT e.id
       , e.name
       , e.age
       , e.department
  FROM employee e
 WHERE e.id = /* id */1
   AND e.name = /* employee.employeeName */'John'
   AND e.age > /* <error descr="The bind variable [hogeCollector] does not exist in the DAO method [selectSubTypeCollector]">hogeCollector</error> */20
