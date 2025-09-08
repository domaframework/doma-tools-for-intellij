/*%if useWith */
WITH
      /*%for cte : cteDefinitions */
      user_stats AS (/*# cte.query */
                     SELECT user_id
                            , COUNT(*) AS order_count
                       FROM orders
                      GROUP BY user_id)
        /*%if cte_has_next */
        ,
        /*%end*/
      /*%end*/
/*%end*/
SELECT u.id
       , u.name
       /*%if useWith */
       , us.order_count
       /*%end*/
  FROM users u
       /*%if useWith */
         /*%for cte : cteDefinitions */
         LEFT JOIN user_stats us
                ON u.id = us.user_id
         /*%end*/
       /*%end*/
UNION ALL
SELECT
       /*%for query : unionQueries */
       /*# query.columns */
       /*%end*/
  FROM /*# tableName */
 WHERE active = /* condition */true
