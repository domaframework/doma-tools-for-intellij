UPDATE employee
   SET /*%populate*/id = id
 WHERE rank < <error descr="Test data is required after a bind variable directive or a literal variable directive">/* employee.rank */</error>
