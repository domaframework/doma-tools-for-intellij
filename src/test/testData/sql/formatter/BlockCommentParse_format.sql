/*%! This is Parser Level Comment*/
SELECT *
  FROM table1
 /*%!This is Parser Level Comment*/
 WHERE column1 = /*# value1 */
   /*+
   * This is Block Comment
   */
   AND column2 = /* value2 */'value2'
    OR column3 = /*^ value3 */'value3'
