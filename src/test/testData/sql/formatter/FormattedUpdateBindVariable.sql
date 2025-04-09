UPDATE /*# tableName */
   SET X1 = 1
       , X2 = 2
       , X3 = 3
       /*%for entity : entities */
       , /*# entity.itemIdentifier */= /* entity.value */'abc'
       /*%end*/
 WHERE X = /* reportId */1 
