/*%! This is Parser Level Comment*/
select * from table1
/*%!This is Parser Level Comment*/
where column1 = /*# value1*/
/*+
   * This is Block Comment
   */
and column2 = /* value2 */ 'value2'     or column3 = /*^ value3 */ 'value3'