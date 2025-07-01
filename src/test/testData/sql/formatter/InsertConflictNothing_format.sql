INSERT INTO users
            (username
             , email)
     VALUES ('user'             
              , 'user@example.com')
ON CONFLICT(username) 
DO NOTHING 
