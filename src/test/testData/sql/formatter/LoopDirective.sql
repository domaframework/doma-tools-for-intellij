SELECT
/*%for column : selectColumns */  /*%if column_index == 0 */
    /* column.name */'id' AS /* column.alias */'user_id'
  /*%else*/   , /* column.name */'name' AS /* column.alias */'user_name'
 /*%end*//*%end*/
FROM users