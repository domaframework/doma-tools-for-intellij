WITH deleted_posts AS (
delete FROM posts
    where deleted = TRUE AND deleted_at < CURRENT_DATE - INTERVAL '30 days'
returning id
)
SELECT * FROM deleted_posts