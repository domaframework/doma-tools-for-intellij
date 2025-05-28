SELECT *
  FROM project_detail
 WHERE detail_id = /* detail.getFirstEmployee().employeeId */0
   /*%if (detail != null && @isNotBlank(detail.projectCategory)) */
   AND detail_id = /* detail.getCustomNumber(detail.projectCategory) */'number'
   /*%for project : projects */
   AND project_id = /* project.id */0
   AND detail_sub_id = /* detail.getCustomNumber(project.projectCategory) */'sub'
   /*%end*/
   /*%end*/
    OR number = /* detail.projectNumber */0
