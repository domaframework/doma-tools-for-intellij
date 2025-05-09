DELETE FROM project
 WHERE rank > 10
   -- projects : Optional<List<Optional<Project>>> -> Project
   AND id = /* projects.projectId */
   -- projects.optionalIds : Optional<List<Optional<Integer>>> -> List<Integer>
/*%for id : projects.<caret>option */
   opt_id = /* id */0
 /*%if id_has_next */
   /*# "OR" */
 /*%end */
/*%end */
