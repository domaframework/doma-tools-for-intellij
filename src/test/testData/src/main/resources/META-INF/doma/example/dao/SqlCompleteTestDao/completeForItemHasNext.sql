select * from principal
where
/*%for item : principal.permissions */
index = /* item_index */0
   /*%if item_has_next */
     OR flag = /* item_has_next.<caret> */false
   /*%end */
/*%end */