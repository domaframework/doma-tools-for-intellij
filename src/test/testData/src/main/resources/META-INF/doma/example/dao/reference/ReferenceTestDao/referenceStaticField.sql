SELECT *
  FROM project_detail
 WHERE category = /* @doma.example.entity.ProjectDetail@projectCategory */'category'
   AND number = /* @doma.example.entity.Project@getFirstEmployee().employeeId.toString() */'9999'
    OR number = /* @doma.example.entity.ProjectDetail@getCustomNumber(detail.projectCategory) */'number'
