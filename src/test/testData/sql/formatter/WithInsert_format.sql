WITH new_user AS (
    INSERT INTO users
                (name
                 , email)
         VALUES ('Alice Example'
                 , 'alice@example.com')
    RETURNING id
              , name
)
INSERT INTO user_profiles
            (user_id
             , bio)
SELECT id
       , name
  FROM new_user 
