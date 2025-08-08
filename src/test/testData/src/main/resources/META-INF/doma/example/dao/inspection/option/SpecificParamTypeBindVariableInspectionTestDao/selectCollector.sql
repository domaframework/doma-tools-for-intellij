SELECT *
  FROM employee
 WHERE salary > /* salary */1000
 and active = /* <error descr="The bind variable [collector] does not exist in the DAO method [selectCollector]">collector</error> */true
