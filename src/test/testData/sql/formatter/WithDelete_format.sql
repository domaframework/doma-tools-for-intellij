WITH deleted_posts AS (
    DELETE FROM posts
     WHERE deleted = TRUE
       AND deleted_at < CURRENT_DATE - INTERVAL '30 days'
 RETURNING id
)
SELECT *
  FROM deleted_posts 
