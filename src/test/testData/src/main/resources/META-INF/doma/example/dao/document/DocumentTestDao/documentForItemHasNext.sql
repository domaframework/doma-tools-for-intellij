select count(*) from principal
where
/*%for item : principal.permissions */
   name = /* item.name */'name'
   /*%if item_has_<caret>next */
    /*# "or" */
   /*%end */
/*%end */