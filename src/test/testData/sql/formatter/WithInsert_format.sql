WITH new_user AS (
    INSERT INTO users
                (name
                 , email)
         VALUES ('Alice Example'
                 , 'alice@example.com')
      RETURNING id
)
INSERT INTO user_profiles
            (user_id
             , bio)
SELECT id
       , 'This is Alice'
       , profile
  FROM new_user 
