WITH recent_activity AS (
    SELECT user_id
           , MAX(logged_in_at) AS last_login
      FROM login_logs
     GROUP BY user_id
)
, updated_users AS (
    UPDATE users u
       SET last_login_at = ra.last_login
      FROM recent_activity ra
     WHERE u.id = ra.user_id
    RETURNING id
)
SELECT *
  FROM updated_users
