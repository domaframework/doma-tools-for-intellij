WITH [abs] AS (
    INSERT INTO employee
                (id
                 , name
                 , "left")
         VALUES ( 1
                  , 'name'
                  , SELECT "age"
                      FROM user
                     WHERE id = 1 )
)
SELECT id
       , [age]
       , "age"
  FROM "order" o
       , `age`
 WHERE (/*%for age : ages */
        "age" = /* age */30
        AND o."Left" = /* left */30
            /*%if age_has_next */
            /*# "and" */
            /*%else */
            /*%end */
        /*%end */)
