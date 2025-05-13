SELECT id
       , name
       , age
  FROM employees
 WHERE
/*%for project : projects */
  id = /* <caret> */0
/*%if project_has_next */
/*# "OR" */
/*%end */
/*%end */ 
