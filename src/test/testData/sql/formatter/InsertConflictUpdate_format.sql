INSERT INTO users
            (username
             , email)
     VALUES ('user'
             , 'user@example.com')
ON CONFLICT(username) ON CONSTRAINT 
DO UPDATE
      SET email = EXCLUDED.email
          , created_at = CURRENT_TIMESTAMP 
