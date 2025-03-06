insert into project_detail (
 project_detail_id
 , project_id
 , project_number
 , project_term_number
 , projec_category)
values (
 /* detail.projectDetailId */1,
 , /* detail.projectId */1
 , /* detail.projectNumber */1
 , /* @doma.example.entity.ProjectDetail@getTermNumber() */'term'
 , /* @doma.example.entity.ProjectDetail@projectCate<caret>gory */'category'
 )