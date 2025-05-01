SELECT *
  FROM employee
 WHERE join_at <= <error descr="Bind variables must be followed by test data">/* referenceDate */</error>
  /*%for project : projects */
         employee_name LIKE /* project.projectName */'hoge'
    /*%if project_has_next */
      /*# "or" */
    /*%end */
  /*%end*/
    OR salary > 1000 
