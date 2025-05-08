select count(*) from principal
where
/*%for item : principal.permissions */
   index = /* item_in<caret>dex */0
   /*%if item_has_next */
    /*# "or" */
   /*%end */
/*%end */