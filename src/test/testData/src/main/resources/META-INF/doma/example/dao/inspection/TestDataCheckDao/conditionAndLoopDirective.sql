SELECT *
  FROM employee
 WHERE join_at <= <error descr="Test data is required after a bind variable directive or a literal variable directive">/* referenceDate */</error>
  /*%for project : projects */
         employee_name LIKE /* project.projectName */'hoge'
    /*%if project_has_next */
      /*%if project.projectName.startsWith("A") */
      /*# "or" */
      /*%else */
        /*! delete comment */
         and name = /* project.projectName */'testName'
          /*# "or" */
      /*%end */
    /*%end */
  /*%end*/
    OR salary > 1000 
