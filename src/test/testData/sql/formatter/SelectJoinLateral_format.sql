SELECT u.user_id
       , u.name
       , o.order_id AS latest_order_id
       , o.order_date
  FROM LATERAL ( SELECT *
                   FROM orders o
                  WHERE o.order_id = u.user_id
                  ORDER BY o.order_date DESC
                  LIMIT 1 ) o u
       LEFT JOIN LATERAL ( SELECT *
                             FROM orders o
                            WHERE o.order_id = u.user_id
                            ORDER BY o.order_date DESC
                            LIMIT 1 ) o
              ON true
             AND false 
