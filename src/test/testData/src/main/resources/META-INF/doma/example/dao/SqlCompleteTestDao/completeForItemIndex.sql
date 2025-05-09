select * from principal
where
/*%for item : principal.permissions */
index = /* item_index.<caret> */0
   /*%if item_has_next */
     OR flag = /* item_has_next */false
   /*%end */
/*%end */