-- Test using Static property as bind variable
update project p
set
    -- Existing field access
    p.project_name = /* project.projectName */'name'
    -- non-existent field access
    , p.project_description = /* project.<error descr="The field or method [projectDescription] does not exist in the class [Project]">projectDescription</error> */'description'
from
    project_term pt
 inner join project_detail pd on p.project_id = pd.project_id
where
    -- instance field access
    p.project_id = /* project.projectId */0
    and p.project_id = pt.project_id
    and pt.start_date >= /* referenceDate */'2099-12-31'
    -- public static field access
    and p.project_number <= /* project.projectNumber */0
    -- private static field access
    and p.project_category = /* project.projectCategory */'category'
    -- public static method access
    and pt.project_term_number = /* project.getTermNumber() */'term'
    -- private static method access
    and pt.project_term_category_name = /* project.<error descr="The field or method [getCategoryName] does not exist in the class [Project]">getCategoryName</error>() */'termCategory'
    -- static field call
    and p.project_status = /* @doma.example.entity.Project@status */'TODO'
    AND (
    -- static method call
    /*%for member : @doma.example.entity.ProjectDetail@members*/
    /*%if member_has_next */
      pd.member_id = /* member.employeeId */0
      /*# "OR"*/
    /*%end */
     /*%end */
    )
    -- Static field call that does not exist
    /*%if @doma.example.entity.ProjectDetail@<error descr="[priority] is not a public or static property in the class [doma.example.entity.ProjectDetail]">priority</error> >= 3 */
    -- Static method call that does not exist
    AND pd.limit_date = /* @doma.example.entity.ProjectDetail@<error descr="[getLimit] is not a public or static property in the class [doma.example.entity.ProjectDetail]">getLimit</error>() */0
    /*%end */