INSERT INTO /*# tableName */
            (x1
             , x2
             /*%for entity : entities */
             , /*# entity.itemIdentifier */
             /*%end*/
             , x3
             , x4)
     VALUES ( /* reportId */1
              , /* reportId */1
              /*%for entity : entities */
              , /* entity.value */'abc'
              /*%end*/
              , /* @userId() */1
              , x5
              , /* @userId() */1
              , x6
              , 1
              , /* @maxDateTime() */'9999-12-31' ) 
