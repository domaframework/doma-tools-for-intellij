select count(*) from principal
where
/*%for proj<caret>ect : @doma.example.entity.Employee@projects */
   name = /* project.projectNumber */'000'
   /*%if project_has_next */
    /*# "or" */
   /*%end */
/*%end */