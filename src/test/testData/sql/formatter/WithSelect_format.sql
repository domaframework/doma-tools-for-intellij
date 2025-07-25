WITH users AS (
    SELECT user_id
           , name
      FROM users
     WHERE name = 'premium'
)
SELECT user_id
       , name
  FROM users
 WHERE suspended = TRUE
