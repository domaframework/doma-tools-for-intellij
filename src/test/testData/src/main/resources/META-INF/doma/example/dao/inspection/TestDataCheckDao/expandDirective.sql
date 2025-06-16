SELECT /*%expand*/* FROM employee
 WHERE rank < <error descr="Test data is required after a bind variable directive or a literal variable directive">/* emplotee.rank */</error>*
