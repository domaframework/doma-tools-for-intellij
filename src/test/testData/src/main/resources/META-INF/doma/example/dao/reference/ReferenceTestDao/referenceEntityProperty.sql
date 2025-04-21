SELECT *
  FROM project_detail
 WHERE detail_id = /* detail.getFirstEmployee().employeeId */0
   /*%for project : projects */
   AND project_id = /* project.id */0
   /*%end*/
OR number = /* detail.projectNumber */