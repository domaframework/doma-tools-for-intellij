SELECT id
       , [age]
       , /*%if maxAge*/
         number + age(/* timestamp1 */'2099-12-31'
                      , /* timestamp2 */'2099-12-31')
         /*%else*/
         name
          ,
         /*%end*/
        "age"
  FROM employee
 WHERE (/*%for age : ages */
        "age" = /* age */30
          /*%if age_has_next */
          /*# "and" */
          /*%else */
          /*%end */
        /*%end */)
   AND (/*%for age : ages */
        abs = /* abs */30
        AND age >= 20
            /*%if age_has_next */
            /*# "or" */
            /*%else */
            /*%end */
        /*%end */)
