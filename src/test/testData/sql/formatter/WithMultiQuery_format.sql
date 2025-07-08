WITH users AS (
    SELECT user_id
           , name
      FROM users
     WHERE name = 'premium'
)
, active_users AS (
    SELECT user_id
           , name
      FROM users
     WHERE last_login > CURRENT_DATE - INTERVAL '30 days'
)
SELECT *
  FROM premium_users
INTERSECT
SELECT *
  FROM active_users
EXCEPT
SELECT user_id
       , name
  FROM users
 WHERE suspended = TRUE
 ORDER BY name ASC
 LIMIT 100 OFFSET 10 
