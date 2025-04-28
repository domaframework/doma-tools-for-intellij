SELECT id
  FROM project_detail
 WHERE category = /* employeesList.get(0).get(0).get(0).projects.get(0).projectCategory */'category'
/*%for employees : employeesList */
  /*%if employees_has_next */
    /*# "OR" */
  /*%end */
  /*%for employee : employees */
     /*%for project : employee.projects */
        project_id = /* project.projectId */
     /*%end */
        number >= /* employee.projects.get(0).projectNumber */9999
    /*%if employee_has_next */
      /*# "AND" */
    /*%end */
  /*%end */
/*%end */