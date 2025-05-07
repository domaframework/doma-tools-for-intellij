select count(*) from principal
where
/*%for i<caret>tem : principal.permissions */
   name = /* item.name */'name'
   /*%if item_has_next */
    /*# "or" */
   /*%end */
/*%end */