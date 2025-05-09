select * from project
where
 -- optionalProjects : Optional<List<Optional<Project>>> -> List<Project>
 -- project : Optional<Project> -> Project
 /*%for pro<caret>ject : optionalProjects */
   -- project.optionalIds : Optional<List<Optional<Integer>>> -> List<Integer>
    -- id : Optional<Integer> -> Integer
   /*%for id : project.optionalIds */
      project_next_id = /* id */0
     /*%if id_has_next */
      /*# "OR" */
     /*%end */
   /*%end */
     project_id = /* project.projectId */0
    /*%if project_has_next */
      /*# "OR" */
    /*%end */
 /*%end */