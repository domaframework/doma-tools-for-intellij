/*%if useWith */
WITH 
/*%for cte : cteDefinitions */
/* cte.name */user_stats AS (
/*# cte.query */SELECT user_id, COUNT(*) as order_count FROM orders GROUP BY user_id
)
  /*%if cte_has_next */,/*%end*/
/*%end*/
/*%end*/
SELECT
u.id,
u.name
  /*%if useWith */
    , us.order_count
  /*%end*/
FROM users u
/*%if useWith */
  /*%for cte : cteDefinitions */
    LEFT JOIN /* cte.name */user_stats us ON u.id = us.user_id
  /*%end*/
/*%end*/

/*%for query : unionQueries */
  /*%if query_index > 0 */
    UNION ALL
  /*%end*/
  SELECT /* query.columns */id, name
  FROM /* query.tableName */users
  WHERE /* query.condition */active = true
/*%end*/