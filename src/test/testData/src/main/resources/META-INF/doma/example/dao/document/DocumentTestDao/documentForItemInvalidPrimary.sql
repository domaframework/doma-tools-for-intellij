select count(*) from principal
where
/*%for item : principal */
name = /* ite<caret>m.name */'name'
   /*%if item_has_next */
/*# "or" */
   /*%end */
/*%end */