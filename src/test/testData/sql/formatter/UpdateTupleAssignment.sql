UPDATE /*# tableName */
 SET (x1
      , x2
      , x3
      /*%for entity : entities */
      , /*# entity.itemIdentifier */ 
      /*%end*/) = ( /* @userId() */1
         , x
         , x + 1
         /*%for entity : entities */
         , /* entity.value */'abc'
         /*%end*/) WHERE x = /* reportId */1 
