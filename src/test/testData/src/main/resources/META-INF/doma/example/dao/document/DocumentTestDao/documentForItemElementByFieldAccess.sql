SELECT id
  FROM project_detail
 WHERE category = 'category'
 -- employeesList -> List<List<Employee>>
/*%for employees : employeesList */
   -- employees_has_next -> boolean
  /*%if employees_has_next */
      /*# "OR" */
  /*%end */
   -- employees -> -> List<Employee>
  /*%for employee : employees */
    /*%for project : employee.projects */
     -- project -> Project
      project_id = /* pr<caret>oject.projectId */
    /*%end */
      number >= /* employee.projects.get(0).projectNumber */9999
    /*%if employee_has_next */
        /*# "AND" */
    /*%end */
  /*%end */
/*%end */ 
