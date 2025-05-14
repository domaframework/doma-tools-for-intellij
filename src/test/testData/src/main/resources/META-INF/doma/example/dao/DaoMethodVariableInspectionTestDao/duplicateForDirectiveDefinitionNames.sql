SELECT *
  FROM users
 WHERE count = /* ids.size() */0
    /*%for member : users */
    OR (id = /* member.userId */0
        AND count < /* users.size() */0)
       AND form = /* inForm */false
    /*%end */
       AND searchName = /* searchName */'search' 
