WITH [abs] AS (
insert into employee(id, name, "left")
values (1, 'name', select "age" from user where id = 1))

SELECT id, [age]
       , "age"
  FROM "order"o, `age`
 WHERE (/*%for age : ages */
        "age" = /* age */30
        AND o."Left" = /* left */30
            /*%if age_has_next */
/*# "and" */
            /*%else */
            /*%end */
        /*%end */)
